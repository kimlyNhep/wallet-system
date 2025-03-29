package com.wlt.payment.consumer;

import com.wlt.payment.dto.FundTransferEvent;
import com.wlt.payment.dto.FundTransferRequestDto;
import com.wlt.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FundTransferConsumer {

//    private final PaymentService paymentService;
//
//    @RabbitListener(queues = "${rabbitmq.queue.gift-code.fund-transfer}")
//    public void handleUserCreatedEvent(FundTransferEvent event) {
//        FundTransferRequestDto requestDto = new FundTransferRequestDto();
//        BeanUtils.copyProperties(event, requestDto);
//        paymentService.fundTransfer(event.getUserId(), requestDto);
//    }
}
