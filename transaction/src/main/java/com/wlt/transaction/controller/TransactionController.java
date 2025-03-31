package com.wlt.transaction.controller;

import com.wlt.transaction.dto.SuccessResponse;
import com.wlt.transaction.dto.TransactionExportRequestDto;
import com.wlt.transaction.dto.TransactionHistoryResponseDto;
import com.wlt.transaction.service.TransactionExportService;
import com.wlt.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionExportService transactionExportService;
    private final TransactionService transactionService;

    @PostMapping(value = "/api/v1/export/csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> exportWalletTransactionsAsCsv(
            @RequestHeader("user-id") Long userId,
            @RequestBody TransactionExportRequestDto request) {

        StreamingResponseBody stream = outputStream -> {
            transactionExportService.exportCSV(userId, request, outputStream);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=wallet_transactions.csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @GetMapping(value = "/api/v1/history")
    public ResponseEntity<SuccessResponse<List<TransactionHistoryResponseDto>>> getTransactionHistories(
            @RequestHeader("user-id") Long userId
    ) {

        SuccessResponse<List<TransactionHistoryResponseDto>> successResponse = new SuccessResponse<>();
        successResponse.setCode("SUCCESS");
        successResponse.setMessage("Success");
        successResponse.setData(transactionService.getTransactionHistory(userId));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
