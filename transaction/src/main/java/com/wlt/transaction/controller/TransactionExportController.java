package com.wlt.transaction.controller;

import com.wlt.transaction.dto.TransactionExportRequestDto;
import com.wlt.transaction.service.TransactionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
public class TransactionExportController {

    private final TransactionExportService transactionExportService;

    @GetMapping(value = "/api/v1/export/csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> exportWalletTransactionsAsCsv(
            TransactionExportRequestDto request) {

        StreamingResponseBody stream = outputStream -> {
            transactionExportService.exportCSV(request, outputStream);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=wallet_transactions.csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }
}
