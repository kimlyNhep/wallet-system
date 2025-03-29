package com.wlt.payment.controller;

import com.wlt.payment.dto.*;
import com.wlt.payment.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping("/api/v1/make-acknowledgement")
    public ResponseEntity<SuccessResponse<TransferAcknowledgementResponseDto>> makeAcknowledgement(@RequestBody TransferAcknowledgementRequestDto acknowledgementRequestDto) {
        SuccessResponse<TransferAcknowledgementResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(fundTransferService.makeAcknowledgement(acknowledgementRequestDto));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
