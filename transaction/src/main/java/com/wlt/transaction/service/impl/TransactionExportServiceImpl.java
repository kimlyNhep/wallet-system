package com.wlt.transaction.service.impl;

import com.opencsv.CSVWriter;
import com.wlt.transaction.dto.TransactionExportRequestDto;
import com.wlt.transaction.entity.TransactionHistory;
import com.wlt.transaction.repository.TransactionHistoryRepository;
import com.wlt.transaction.service.TransactionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportServiceImpl implements TransactionExportService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public void exportCSV(Long userId, TransactionExportRequestDto request, OutputStream outputStream) {
        try {
            // 1. Create CSV Writer with proper encoding
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            // 2. Write CSV Header
            String[] header = {
                    "Transaction Id",
                    "Amount",
                    "Credit Amount",
                    "Credit Currency",
                    "Debit Amount",
                    "Credit Wallet Id",
                    "Debit Wallet Id",
                    "Debit Currency",
                    "SaveTimestamp",
                    "Transaction Reference NO",
                    "Transaction Type",
                    "Transaction Ccy",
                    "User Id",
                    "Status"
            };
            csvWriter.writeNext(header);

            // 3. Stream data from database
            List<TransactionHistory> transactionHistories = transactionHistoryRepository.findByUserIdAndSaveTimestampAfterAndSaveTimestampBefore(userId,
                    request.getStartDate().atStartOfDay(), request.getEndDate().plusDays(1).atStartOfDay());
            transactionHistories.forEach(transactionHistory -> {
                String[] row = new String[header.length];
                row[0] = String.valueOf(transactionHistory.getId());
                row[1] = transactionHistory.getAmount().toString();
                row[2] = transactionHistory.getCrAmount().toString();
                row[3] = transactionHistory.getCrCcy();
                row[4] = transactionHistory.getDrAmount() != null ? transactionHistory.getDrAmount().toString() : "";
                row[5] = transactionHistory.getCrWalletId().toString();
                row[6] = transactionHistory.getDrWalletId() != null ? transactionHistory.getDrWalletId().toString() : "";
                row[7] = transactionHistory.getDrCcy();
                row[8] = transactionHistory.getSaveTimestamp().toString();
                row[9] = transactionHistory.getTransactionRefNo();
                row[10] = transactionHistory.getTransactionType();
                row[11] = transactionHistory.getUserId().toString();
                row[12] = transactionHistory.getStatus();
                csvWriter.writeNext(row);
            });

            // 4. Flush and close resources
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV file", e);
        }
    }
}
