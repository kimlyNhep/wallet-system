package com.wlt.payment.controller;

import com.wlt.payment.dto.GiftCodeValidationResponseDto;
import com.wlt.payment.dto.SuccessResponse;
import com.wlt.payment.service.TopUpBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopUpBalanceController {

    private final TopUpBalanceService topUpBalanceService;

    @GetMapping("/api/v1/validate/gift-code/{code}")
    public ResponseEntity<SuccessResponse<GiftCodeValidationResponseDto>> validateGiftCode(
            @PathVariable("code") String code
    ) {
        SuccessResponse<GiftCodeValidationResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(topUpBalanceService.validateGiftCode(code));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
