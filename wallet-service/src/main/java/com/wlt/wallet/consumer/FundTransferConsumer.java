package com.wlt.wallet.consumer;

import com.wlt.wallet.dto.FundTransferEvent;
import com.wlt.wallet.dto.FundTransferResponseDto;
import com.wlt.wallet.dto.InitFundTransferRequestDto;
import com.wlt.wallet.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FundTransferConsumer {

    private final FundTransferService fundTransferService;

//    @RabbitListener(queues = "${rabbitmq.queue.fund-transfer}")
//    public void handleUpdateAccountBalance(FundTransferEvent event) {
//        log.info("Received FundTransferEvent: {}", event);
//        InitFundTransferRequestDto fundTransferRequestDto = new InitFundTransferRequestDto();
//        BeanUtils.copyProperties(event, fundTransferRequestDto);
//        fundTransferService.fundTransfer(event.getUserId(), fundTransferRequestDto);
//    }
}
