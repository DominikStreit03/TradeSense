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

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        //TODO: Identify unique trades based on symbol, buyInKurs, date and time
         try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                } // Skip header
                try {
                    String symbol = row.getCell(0).getStringCellValue();
                    Double buyInKurs = row.getCell(1).getNumericCellValue();
                    Double takeProfitKurs = row.getCell(2).getNumericCellValue();
                    Double menge = row.getCell(3).getNumericCellValue();
                    LocalDate datum = row.getCell(4).getLocalDateTimeCellValue().toLocalDate();
                    LocalTime uhrzeit = row.getCell(5).getLocalDateTimeCellValue().toLocalTime();
                    String key = symbol + ":" + buyInKurs + ":" + datum + ":" + uhrzeit;
                    if (!uniqueKeys.contains(key)) {
                        Trade trade = new Trade();
                        trade.setSymbol(symbol);
                        trade.setBuyIn(buyInKurs);
                        trade.setSellOut(takeProfitKurs);
                        trade.setAmount(menge);
//                        trade.setDatum(datum);
//                        trade.setUhrzeit(uhrzeit);
                        tradeRepository.save(trade);
                        uniqueKeys.add(key);
                        logger.info("Trade gespeichert: {}", key);
                    } else {
                        logger.info("Trade übersprungen (Duplikat): {}", key);
                    }
                } catch (Exception rowEx) {
                    logger.error("Fehler beim Verarbeiten der Zeile {}: {}", row.getRowNum(), rowEx.getMessage(), rowEx);
                }
            }
        } catch (Exception e) {
            logger.error("Fehler beim Einlesen der Excel-Datei: {}", e.getMessage(), e);
            throw e;
        }
    }
}
