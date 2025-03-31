package com.wlt.payment.service.impl;

import com.wlt.payment.config.ApplicationPropertyConfig;
import com.wlt.payment.constants.MessageError;
import com.wlt.payment.constants.Status;
import com.wlt.payment.dto.*;
import com.wlt.payment.entity.Transaction;
import com.wlt.payment.exception.CustomException;
import com.wlt.payment.provider.ServiceProvider;
import com.wlt.payment.repository.TransactionRepository;
import com.wlt.payment.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FundTransferServiceImpl extends ApplicationPropertyConfig implements FundTransferService {

    private final TransactionRepository transactionRepository;
    private final ServiceProvider serviceProvider;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public FundTransferResponseDto initFundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto) {
        Transaction initTransaction = getInitTransaction(fundTransferRequestDto.getPaymentRefNo(), fundTransferRequestDto);
        transactionRepository.save(initTransaction);

        WalletAccountResponseDto drWalletResponse = serviceProvider.getWalletAccount(userId, fundTransferRequestDto.getDrWalletId());
        if (drWalletResponse != null) {
            BigDecimal debitAmount = drWalletResponse.getBalance();
            if (debitAmount.compareTo(fundTransferRequestDto.getAmount()) < 0) {
                throw new CustomException(MessageError.ERR_001_INSUFFICIENT_AMOUNT);
            }
        } else {
            throw new CustomException(MessageError.ERR_002_WALLET_NOT_ENOUGH);
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
            throw new CustomException(MessageError.ERR_009_MAKE_ACKNOWLEDGEMENT_FAILED);
        }

        FundTransferResponseDto responseDto = new FundTransferResponseDto();
        responseDto.setCcy(fundTransferRequestDto.getCcy());
        responseDto.setAmount(fundTransferRequestDto.getAmount());
        return responseDto;
    }

    @Override
    public void fundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto) {
        DebitAccountBalanceRequestDto detailsRequestDto = new DebitAccountBalanceRequestDto();
        detailsRequestDto.setCcy(fundTransferRequestDto.getCcy());
        detailsRequestDto.setDebitBalance(fundTransferRequestDto.getAmount());
        detailsRequestDto.setDebitWalletId(fundTransferRequestDto.getDrWalletId());
        AccountBalanceResponseDto debitBalanceResponseEntity = serviceProvider.debitAccountBalance(userId, detailsRequestDto);

        if (debitBalanceResponseEntity != null) {
            CreditAccountBalanceRequestDto creditAccountBalanceRequestDto = new CreditAccountBalanceRequestDto();
            creditAccountBalanceRequestDto.setCcy(fundTransferRequestDto.getCcy());
            creditAccountBalanceRequestDto.setCreditBalance(fundTransferRequestDto.getAmount());
            creditAccountBalanceRequestDto.setCreditWalletId(fundTransferRequestDto.getCrWalletId());
            AccountBalanceResponseDto creditBalanceResponseEntity = serviceProvider.creditAccountBalance(userId, creditAccountBalanceRequestDto);

            if (creditBalanceResponseEntity != null) {
                String creditBalanceStatus = creditBalanceResponseEntity.getStatus();
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
                            transactionHistoryEvent.setCrAmount(creditBalanceResponseEntity.getBalance());
                            transactionHistoryEvent.setCrCcy(creditBalanceResponseEntity.getCcy());
                            transactionHistoryEvent.setDrAmount(debitBalanceResponseEntity.getBalance());
                            transactionHistoryEvent.setDrCcy(debitBalanceResponseEntity.getCcy());
                            transactionHistoryEvent.setTransactionRefNo(tranRefNo);
                            transactionHistoryEvent.setStatus(Status.SUCCESS);
                            transactionHistoryEvent.setExchangeRate(creditBalanceResponseEntity.getExchangeRate());
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
                        return;
                    }
                }
            }
        }

        throw new CustomException(MessageError.SOMETHING_WENT_WRONG);
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
            throw new CustomException(MessageError.ERR_007_TRANSACTION_NOT_FOUND);
        }
    }

    @Override
    public TransactionStatusResponseDto transactionStatus(TransferAcknowledgementRequestDto transferAcknowledgementRequestDto) {
        Optional<Transaction> transaction = transactionRepository.findByPaymentRefNo(transferAcknowledgementRequestDto.getPaymentRefNo());
        if (transaction.isPresent()) {
            TransactionStatusResponseDto responseDto = new TransactionStatusResponseDto();
            responseDto.setStatus(transaction.get().getStatus());
            responseDto.setPaymentRefNo(transaction.get().getPaymentRefNo());
            responseDto.setTransactionRefNo(transaction.get().getTransactionRefNo());
            return responseDto;
        }

        throw new CustomException(MessageError.ERR_007_TRANSACTION_NOT_FOUND);
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
