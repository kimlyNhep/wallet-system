package com.wlt.transaction.service.impl;

import com.opencsv.CSVWriter;
import com.wlt.transaction.dto.TransactionExportRequestDto;
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

    private final JdbcTemplate jdbcTemplate;

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
            String sql = buildQuery(request);

            jdbcTemplate.query(sql, ps -> {
                if (request.getStartDate() != null) ps.setDate(1, Date.valueOf(request.getStartDate()));
                if (request.getEndDate() != null) ps.setDate(2, Date.valueOf(request.getEndDate()));
                if (request.getTransactionType() != null) ps.setString(3, request.getTransactionType());
            }, rs -> {
                String[] row = new String[header.length];
                row[0] = rs.getString("transaction_id");
                row[1] = rs.getTimestamp("transaction_date").toLocalDateTime().toString();
                row[2] = rs.getBigDecimal("amount").toPlainString();
                row[3] = rs.getString("currency");
                row[4] = rs.getString("transaction_type");
                row[5] = rs.getString("status");
                row[6] = rs.getString("description");

                csvWriter.writeNext(row);
            });

            // 4. Flush and close resources
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV file", e);
        }
    }

    private String buildQuery(TransactionExportRequestDto request) {
        String baseQuery = """
                    SELECT transaction_ref_no, user_id, save_timestamp, status,
                                                transaction_type, dr_amount, dr_ccy, dr_wallet_id,
                                                cr_amount, cr_ccy, cr_wallet_id, txn_ccy, exchange_rate
                                         FROM wlt_transaction_history
                                         WHERE 1=1
                """;

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (request.getStartDate() != null) {
            conditions.add("transaction_date >= ?");
            params.add(request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            conditions.add("transaction_date < ?");
            params.add(request.getEndDate().plusDays(1).atStartOfDay());
        }
        if (request.getTransactionType() != null) {
            conditions.add("transaction_type = ?");
            params.add(request.getTransactionType());
        }

        return baseQuery + (conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions));
    }
}
