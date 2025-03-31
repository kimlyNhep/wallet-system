package com.wlt.payment.service.impl;

import com.wlt.payment.constants.Status;
import com.wlt.payment.dto.*;
import com.wlt.payment.entity.Transaction;
import com.wlt.payment.repository.TransactionRepository;
import com.wlt.payment.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl implements FundTransferService {

    private final TransactionRepository transactionRepository;

    @Value("${wallet.service.base-url}")
    private String walletServiceUrl;

    @Value("${rabbitmq.exchange.fund-transfer}")
    private String fundTransferExchange;

    @Value("${rabbitmq.routing-key.fund-transfer}")
    private String fundTransferRoutingKey;

    @Value("${rabbitmq.exchange.transaction.history}")
    private String transactionHistoryExchange;

    @Value("${rabbitmq.routing-key.transaction.history}")
    private String transactionHistoryRoutingKey;

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public FundTransferResponseDto initFundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Collation-id", UUID.randomUUID().toString());
        headers.add("user-id", String.valueOf(userId));

        Transaction initTransaction = getInitTransaction(fundTransferRequestDto.getPaymentRefNo(), fundTransferRequestDto);
        transactionRepository.save(initTransaction);

        ResponseEntity<SuccessResponse<WalletAccountResponseDto>> drWalletResponseEntity = restTemplate.exchange(
                walletServiceUrl + "/api/v1/account/" + fundTransferRequestDto.getDrWalletId(),
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (drWalletResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<WalletAccountResponseDto> drWalletResponse = drWalletResponseEntity.getBody();
            if (drWalletResponse != null && drWalletResponse.getData() != null) {
                BigDecimal debitAmount = drWalletResponse.getData().getBalance();
                if (debitAmount.compareTo(fundTransferRequestDto.getAmount()) < 0) {
                    throw new RuntimeException("Insufficient amount");
                }
            } else {
                throw new RuntimeException("debit wallet not found");
            }
        }
        TransferAcknowledgementRequestDto acknowledgementRequestDto = new TransferAcknowledgementRequestDto();
        acknowledgementRequestDto.setPaymentRefNo(fundTransferRequestDto.getPaymentRefNo());
        TransferAcknowledgementResponseDto acknowledgementResponseDto = makeAcknowledgement(acknowledgementRequestDto);
        if (acknowledgementResponseDto != null && acknowledgementResponseDto.getStatus().equals("APPROVED")) {
            FundTransferEvent fundTransferEvent = new FundTransferEvent();
            BeanUtils.copyProperties(fundTransferRequestDto, fundTransferEvent);
            fundTransferEvent.setUserId(userId);
            rabbitTemplate.convertAndSend(fundTransferExchange, fundTransferRoutingKey, fundTransferEvent);
        } else {
            throw new RuntimeException("Transaction acknowledgement failed");
        }

        FundTransferResponseDto responseDto = new FundTransferResponseDto();
        responseDto.setCcy(fundTransferRequestDto.getCcy());
        responseDto.setAmount(fundTransferRequestDto.getAmount());
        return responseDto;
    }

    @Override
    public ConfirmFundTransferResponseDto fundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Collation-id", UUID.randomUUID().toString());
        headers.add("user-id", String.valueOf(userId));

        DebitAccountBalanceRequestDto detailsRequestDto = new DebitAccountBalanceRequestDto();
        detailsRequestDto.setCcy(fundTransferRequestDto.getCcy());
        detailsRequestDto.setDebitBalance(fundTransferRequestDto.getAmount());
        detailsRequestDto.setDebitWalletId(fundTransferRequestDto.getDrWalletId());
        ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> debitBalanceResponseEntity = restTemplate.exchange(
                walletServiceUrl + "/api/v1/account/debit",
                HttpMethod.POST,
                new HttpEntity<>(detailsRequestDto, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        if (debitBalanceResponseEntity.getStatusCode().is2xxSuccessful()) {
            SuccessResponse<AccountBalanceResponseDto> debitBalanceResponseDto = debitBalanceResponseEntity.getBody();
            if (debitBalanceResponseDto != null && debitBalanceResponseDto.getData() != null) {
                CreditAccountBalanceRequestDto creditAccountBalanceRequestDto = new CreditAccountBalanceRequestDto();
                creditAccountBalanceRequestDto.setCcy(fundTransferRequestDto.getCcy());
                creditAccountBalanceRequestDto.setCreditBalance(fundTransferRequestDto.getAmount());
                creditAccountBalanceRequestDto.setCreditWalletId(fundTransferRequestDto.getCrWalletId());
                ResponseEntity<SuccessResponse<AccountBalanceResponseDto>> creditBalanceResponseEntity = restTemplate.exchange(
                        walletServiceUrl + "/api/v1/account/credit",
                        HttpMethod.POST,
                        new HttpEntity<>(creditAccountBalanceRequestDto, headers),
                        new ParameterizedTypeReference<>() {
                        }
                );

                if (creditBalanceResponseEntity.getStatusCode().is2xxSuccessful()) {
                    SuccessResponse<AccountBalanceResponseDto> creditBalanceResponse = creditBalanceResponseEntity.getBody();
                    if (creditBalanceResponse != null && creditBalanceResponse.getData() != null) {
                        String creditBalanceStatus = creditBalanceResponse.getData().getStatus();
                        if ("SUCCESS".equals(creditBalanceStatus)) {
                            String tranRefNo = UUID.randomUUID().toString();
                            Optional<Transaction> successTransactionOptional = transactionRepository.findByPaymentRefNoAndStatus(fundTransferRequestDto.getPaymentRefNo(), Status.PENDING);
                            if (successTransactionOptional.isPresent()) {
                                Transaction sucessTransaction = successTransactionOptional.get();
                                sucessTransaction.setStatus(Status.SUCCESS);
                                sucessTransaction.setTransactionRefNo(tranRefNo);
                                transactionRepository.save(sucessTransaction);

                                CompletableFuture.runAsync(() -> {
                                    TransactionHistoryEvent transactionHistoryEvent = new TransactionHistoryEvent();
                                    transactionHistoryEvent.setCrAmount(creditBalanceResponse.getData().getBalance());
                                    transactionHistoryEvent.setCrCcy(creditBalanceResponse.getData().getCcy());
                                    transactionHistoryEvent.setDrAmount(debitBalanceResponseDto.getData().getBalance());
                                    transactionHistoryEvent.setDrCcy(debitBalanceResponseDto.getData().getCcy());
                                    transactionHistoryEvent.setTransactionRefNo(tranRefNo);
                                    transactionHistoryEvent.setStatus(Status.SUCCESS);
                                    transactionHistoryEvent.setExchangeRate(creditBalanceResponse.getData().getExchangeRate());
                                    transactionHistoryEvent.setAmount(fundTransferRequestDto.getAmount());
                                    transactionHistoryEvent.setTxnCcy(fundTransferRequestDto.getCcy());
                                    transactionHistoryEvent.setSaveTimestamp(LocalDateTime.now());
                                    transactionHistoryEvent.setUserId(userId);
                                    transactionHistoryEvent.setCrWalletId(fundTransferRequestDto.getCrWalletId());
                                    transactionHistoryEvent.setDrWalletId(fundTransferRequestDto.getDrWalletId());
                                    transactionHistoryEvent.setTransactionType("FUND_TRANSFER");
                                    rabbitTemplate.convertAndSend(transactionHistoryExchange, transactionHistoryRoutingKey, transactionHistoryEvent);
                                });

                                ConfirmFundTransferResponseDto responseDto = new ConfirmFundTransferResponseDto();
                                responseDto.setPaymentRefNo(fundTransferRequestDto.getPaymentRefNo());
                                responseDto.setTransactionRefNo(tranRefNo);
                                return responseDto;
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("credit wallet not found");
                }
            }
        } else {
            // Refund
            throw new RuntimeException("credit wallet not found");
        }

        throw new RuntimeException("something went wrong");
    }

    @Override
    public TransferAcknowledgementResponseDto makeAcknowledgement(TransferAcknowledgementRequestDto transferAcknowledgementRequestDto) {
        Optional<Transaction> transactionOptional = transactionRepository.findByPaymentRefNoAndStatus(transferAcknowledgementRequestDto.getPaymentRefNo(), Status.INIT);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            transaction.setStatus(Status.PENDING);
            transactionRepository.save(transaction);

            TransferAcknowledgementResponseDto responseDto = new TransferAcknowledgementResponseDto();
            responseDto.setStatus("APPROVED");
            responseDto.setPaymentRefNo(transaction.getPaymentRefNo());
            return responseDto;
        } else {
            throw new RuntimeException("Transaction not found");
        }
    }

    private Transaction getInitTransaction(String paymentRefNo, FundTransferRequestDto fundTransferRequestDto) {
        Transaction transaction = new Transaction();
        transaction.setAmount(fundTransferRequestDto.getAmount());
        transaction.setCrCcy(fundTransferRequestDto.getCrCcy());
        transaction.setDrCcy(fundTransferRequestDto.getDrCcy());
        transaction.setCcy(fundTransferRequestDto.getCcy());
        transaction.setCrWalletId(fundTransferRequestDto.getCrWalletId());
        transaction.setDrWalletId(fundTransferRequestDto.getDrWalletId());
        transaction.setExchangeRate(BigDecimal.ZERO);
        transaction.setStatus(Status.INIT);
        transaction.setPaymentRefNo(paymentRefNo);
        return transaction;
    }
}
