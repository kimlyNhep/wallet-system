package com.wlt.payment.consumer;

import com.wlt.payment.dto.FundTransferEvent;
import com.wlt.payment.dto.FundTransferRequestDto;
import com.wlt.payment.service.FundTransferService;
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

    @RabbitListener(queues = "${rabbitmq.queue.fund-transfer}")
    public void handleUserCreatedEvent(FundTransferEvent event) {
        try {
            FundTransferRequestDto requestDto = new FundTransferRequestDto();
            BeanUtils.copyProperties(event, requestDto);
            fundTransferService.fundTransfer(event.getUserId(), requestDto);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
