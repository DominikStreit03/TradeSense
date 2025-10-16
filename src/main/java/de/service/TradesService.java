package de.service;

import de.model.Trade;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TradesService {
    List<Trade> getAllTrades();
    Trade saveTrade(Trade trade);
    void importTradesFromExcel(MultipartFile file) throws Exception;
    void importTradesFromCsv(MultipartFile file) throws Exception;

}
