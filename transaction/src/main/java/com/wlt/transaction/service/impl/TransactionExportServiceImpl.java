package com.wlt.transaction.service.impl;

import com.opencsv.CSVWriter;
import com.wlt.transaction.dto.TransactionExportRequestDto;
import com.wlt.transaction.entity.TransactionHistory;
import com.wlt.transaction.repository.TransactionHistoryRepository;
import com.wlt.transaction.service.TransactionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportServiceImpl implements TransactionExportService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public void exportCSV(TransactionExportRequestDto request, OutputStream outputStream) {
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
                    "Transaction ID",
                    "Date",
                    "Amount",
                    "Currency",
                    "Type",
                    "Status",
                    "Description"
            };
            csvWriter.writeNext(header);

            // 3. Stream data from database
            List<TransactionHistory> transactionHistories = transactionHistoryRepository.findAll();
            transactionHistories.forEach(transactionHistory -> {
                String[] row = new String[header.length];
                row[0] = String.valueOf(transactionHistory.getId());
                row[1] = transactionHistory.getSaveTimestamp().toString();
                row[2] = transactionHistory.getAmount().toString();
                row[3] = transactionHistory.getTxnCcy();
                row[4] = transactionHistory.getTransactionType();
                row[5] = transactionHistory.getStatus();
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
