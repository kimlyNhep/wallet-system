package com.wlt.transaction.service;

import com.wlt.transaction.dto.TransactionExportRequestDto;

import java.io.OutputStream;

public interface TransactionExportService {
    void exportCSV(Long userId, TransactionExportRequestDto request, OutputStream outputStream);
}
