package com.wlt.wallet.controller;

import com.wlt.wallet.dto.FundTransferResponseDto;
import com.wlt.wallet.dto.InitFundTransferRequestDto;
import com.wlt.wallet.dto.SuccessResponse;
import com.wlt.wallet.service.FundTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping("/api/v1/fund-transfer")
    public ResponseEntity<SuccessResponse<FundTransferResponseDto>> makeFundTransfer(
            @RequestHeader("user-id") Long userId,
            @RequestBody InitFundTransferRequestDto requestDto
            ) {
        SuccessResponse<FundTransferResponseDto> successResponse = new SuccessResponse<>();
        successResponse.setMessage("success");
        successResponse.setData(fundTransferService.initFundTransfer(userId, requestDto));
        return ResponseEntity.ok(successResponse);
    }
}
