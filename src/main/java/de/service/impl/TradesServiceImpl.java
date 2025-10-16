package de.service.impl;

import de.model.Trade;
import de.repository.TradeRepository;
import de.service.TradesService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TradesServiceImpl implements TradesService {

    private static final Logger logger = LoggerFactory.getLogger(TradesServiceImpl.class);

    private final TradeRepository tradeRepository;

    @Autowired
    public TradesServiceImpl(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }


    @Override
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @Override
    public Trade saveTrade(Trade trade) {
        return tradeRepository.save(trade);
    }

    @Override
    public void importTradesFromExcel(MultipartFile file) throws Exception {
        Set<String> uniqueKeys = new HashSet<>();
        List<Trade> existingTrades = tradeRepository.findAll();

        // add existing trades to duplicate map
        for (Trade t : existingTrades) {
            String key = t.getSymbol() + ":" + t.getEntryPrice();
            uniqueKeys.add(key);
        }

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                try {
                    if (row == null) continue;

                    Cell symbolCell = row.getCell(0);
                    Cell entryCell = row.getCell(1);
                    Cell exitCell = row.getCell(2);
                    Cell qtyCell = row.getCell(3);
                    Cell dateCell = row.getCell(4);
                    Cell timeCell = row.getCell(5);

                    if (symbolCell == null || entryCell == null || exitCell == null || qtyCell == null)
                        continue;

                    String symbol = symbolCell.getStringCellValue();
                    Double entry = entryCell.getNumericCellValue();
                    Double exit = exitCell.getNumericCellValue();
                    Double quantity = qtyCell.getNumericCellValue();

                    // for test purposes: duplicate definition a bit looser (without date/time)

                    String key = symbol + ":" + entry;

                    if (!uniqueKeys.contains(key)) {
                        Trade trade = new Trade();
                        trade.setSymbol(symbol != null ? symbol : "UNKNOWN");
                        trade.setEntryPrice(entry);
                        trade.setExitPrice(exit);
                        trade.setQuantity(quantity);

                        tradeRepository.save(trade);
                        uniqueKeys.add(key);
                        logger.info("Trade saved: {}", key);
                    } else {
                        logger.info("Trade skipped (duplicat): {}", key);
                    }

                } catch (Exception rowEx) {
                    logger.error("Error while processing row {}: {}", row.getRowNum(), rowEx.getMessage(), rowEx);
                }
            }
        } catch (Exception e) {
            logger.error("Error while reading excel File: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    public void importTradesFromCsv(MultipartFile file) throws Exception {
        // CSV-Format expected (Header): symbol,entryPrice,exitPrice,quantity,profitLoss,timestamp,tags,notes
        // timestamp im ISO_LOCAL_DATE_TIME Format z.B. 2023-08-01T15:30:00
        Set<String> uniqueKeys = new HashSet<>();
        List<Trade> existingTrades = tradeRepository.findAll();
        for (Trade t : existingTrades) {
            if (t.getSymbol() != null && t.getEntryPrice() != null && t.getTimestamp() != null) {
                uniqueKeys.add(t.getSymbol() + ":" + t.getEntryPrice() + ":" + t.getTimestamp().toString());
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                logger.warn("Retrieve empty CSV-File.");
                return;
            }
            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                idx.put(headers[i].trim().replaceAll("\"", ""), i);
            }

            String line;
            int rowNum = 1;
            while ((line = br.readLine()) != null) {
                rowNum++;
                try {
                    // Split with CSV-safe regex
                    String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    String symbol = getByIndex(tokens, idx.get("symbol"));
                    Double entryPrice = parseDouble(getByIndex(tokens, idx.get("entryPrice")));
                    Double exitPrice = parseDouble(getByIndex(tokens, idx.get("exitPrice")));
                    Double quantity = parseDouble(getByIndex(tokens, idx.get("quantity")));
                    Double profitLoss = parseDouble(getByIndex(tokens, idx.get("profitLoss")));
                    String timestampStr = getByIndex(tokens, idx.get("timestamp"));
                    LocalDateTime timestamp = null;
                    if (timestampStr != null && !timestampStr.isEmpty()) {
                        try {
                            timestamp = LocalDateTime.parse(timestampStr, dtf);
                        } catch (Exception ex) {
                            // Fallback: try space-separated pattern
                            try {
                                timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), dtf);
                            } catch (Exception ex2) {
                                timestamp = LocalDateTime.now();
                            }
                        }
                    } else {
                        timestamp = LocalDateTime.now();
                    }

                    // Compute profitLoss if not provided and other fields available
                    if (profitLoss == null && entryPrice != null && exitPrice != null && quantity != null) {
                        profitLoss = (exitPrice - entryPrice) * quantity;
                    }

                    // Tags separated by ; or ,
                    String tagsRaw = getByIndex(tokens, idx.get("tags"));
                    Set<String> tags = new HashSet<>();
                    if (tagsRaw != null && !tagsRaw.isEmpty()) {
                        String[] ts = tagsRaw.split("[;|,]");
                        for (String t : ts) {
                            String s = t.trim().replaceAll("^\"|\"$", "");
                            if (!s.isEmpty()) tags.add(s);
                        }
                    }

                    String notes = getByIndex(tokens, idx.get("notes"));
                    if (notes != null) {
                        notes = notes.replaceAll("^\"|\"$", "");
                    }

                    String key = symbol + ":" + (entryPrice != null ? entryPrice.toString() : "null") + ":" + timestamp.toString();
                    if (!uniqueKeys.contains(key)) {
                        Trade trade = new Trade();
                        trade.setSymbol(symbol);
                        trade.setEntryPrice(entryPrice);
                        trade.setExitPrice(exitPrice);
                        trade.setQuantity(quantity);
                        trade.setProfitLoss(entryPrice, exitPrice, quantity);
                        trade.setTimestamp(timestamp);
                        trade.setTags(tags);
                        trade.setNotes(notes);
                        tradeRepository.save(trade);
                        uniqueKeys.add(key);
                        logger.info("CSV-Trade saved (row {}): {}", rowNum, key);
                    } else {
                        logger.info("CSV-Trade skipped (duplicat) row {}: {}", rowNum, key);
                    }
                } catch (Exception ex) {
                    logger.error("Error while processing CSV-row {}: {}", rowNum, ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            logger.error("Error while reading CSV-file: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Helfer-Methoden
    private String getByIndex(String[] tokens, Integer i) {
        if (i == null) return null;
        if (i < 0 || i >= tokens.length) return null;
        String v = tokens[i].trim();
        if (v.isEmpty()) return null;
        return v.replaceAll("^\"|\"$", "");
    }

    private Double parseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }
}
