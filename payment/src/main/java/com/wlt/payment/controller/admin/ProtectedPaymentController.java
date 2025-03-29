package com.wlt.payment.controller.admin;

import com.wlt.payment.dto.GenerateGiftCodeRequestDto;
import com.wlt.payment.dto.GenerateGiftCodeResponseDto;
import com.wlt.payment.dto.SuccessResponse;
import com.wlt.payment.service.TopUpBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProtectedPaymentController {
    private final TopUpBalanceService topUpBalanceService;

    @PostMapping("/api/v1/protected/gift-code/generate")
    public ResponseEntity<SuccessResponse<GenerateGiftCodeResponseDto>> generateGiftCode(
            @RequestBody GenerateGiftCodeRequestDto requestDto
            ) {
        SuccessResponse<GenerateGiftCodeResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(topUpBalanceService.generateGiftCode(requestDto));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
