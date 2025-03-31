package com.wlt.wallet.service.impl;

import com.wlt.wallet.constants.CommonConstants;
import com.wlt.wallet.constants.MessageError;
import com.wlt.wallet.dto.GiftCodeRedeemEvent;
import com.wlt.wallet.dto.RedeemCodeResponseDto;
import com.wlt.wallet.dto.RedeemGiftCodeRequestDto;
import com.wlt.wallet.dto.RedeemGiftCodeResponseDto;
import com.wlt.wallet.entity.WalletAccount;
import com.wlt.wallet.exception.CustomException;
import com.wlt.wallet.provider.ServiceProvider;
import com.wlt.wallet.repository.WalletAccountRepository;
import com.wlt.wallet.service.GiftCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiftCodeServiceImpl implements GiftCodeService {

    private final ServiceProvider serviceProvider;
    private final WalletAccountRepository walletAccountRepository;

    @Override
    public RedeemGiftCodeResponseDto redeemGiftCode(Long userId, RedeemGiftCodeRequestDto requestDto) {
        Optional<WalletAccount> walletAccountOptional = walletAccountRepository.findByIdAndStatus(requestDto.getCreditWalletId(), CommonConstants.ACTIVE);
        if (walletAccountOptional.isPresent()) {
            GiftCodeRedeemEvent giftCodeRedeemEvent = new GiftCodeRedeemEvent();
            giftCodeRedeemEvent.setUserId(userId);
            giftCodeRedeemEvent.setGiftCode(requestDto.getGiftCode());
            giftCodeRedeemEvent.setCreditWalletId(requestDto.getCreditWalletId());

            RedeemCodeResponseDto redeemCodeResponse = serviceProvider.redeemGiftCode(userId, giftCodeRedeemEvent);

            if (redeemCodeResponse != null) {
                String redeemStatus = redeemCodeResponse.getStatus();
                if ("REDEEMED".equals(redeemStatus)) {
                    RedeemGiftCodeResponseDto response = new RedeemGiftCodeResponseDto();
                    response.setGiftCode(requestDto.getGiftCode());
                    response.setDescription("Success redeem code");
                    return response;
                }
            }
        }

        throw new CustomException(MessageError.ERR_003_INVALID_GIFT_CODE);
    }
}
