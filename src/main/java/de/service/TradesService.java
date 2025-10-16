package de.service;

import de.model.trade.Trade;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface TradesService {
    List<Trade> getAllTrades();
    Trade saveTrade(Trade trade);
    void importTradesFromExcel(MultipartFile file) throws Exception;
    void importTradesFromCsv(MultipartFile file) throws Exception;

}
