package com.wlt.payment.controller;

import com.wlt.payment.dto.*;
import com.wlt.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/v1/validate/gift-code/{code}")
    public ResponseEntity<SuccessResponse<GiftCodeValidationResponseDto>> validateGiftCode(
            @PathVariable("code") String code
    ) {
        SuccessResponse<GiftCodeValidationResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(paymentService.validateGiftCode(code));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/api/v1/exchange-rate")
    public ResponseEntity<SuccessResponse<GetExchangeRateAmountDto>> getExchangeRateAmount(
            @RequestBody GetExchangeRateAmountRequestDto getExchangeRateAmountRequestDto
    ) {
        SuccessResponse<GetExchangeRateAmountDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(paymentService.getExchangeRateAmount(getExchangeRateAmountRequestDto));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

//    @PostMapping("/api/v1/fund-transfer/init")
//    public ResponseEntity<SuccessResponse<FundTransferResponseDto>> makeFundTransfer(
//            @RequestBody FundTransferRequestDto fundTransferRequestDto) {
//        SuccessResponse<FundTransferResponseDto> successResponse = new SuccessResponse<>();
//        successResponse.setMessage("success");
//        successResponse.setData(paymentService.fundTransfer(fundTransferRequestDto));
//        return new ResponseEntity<>(successResponse, HttpStatus.OK);
//    }
}
