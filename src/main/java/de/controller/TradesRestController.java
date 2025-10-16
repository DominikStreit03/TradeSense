package de.controller;

import de.model.trade.Trade;
import de.service.TradesService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST-API für Trades: liefert Trades, erlaubt Upload von CSV und liefert Grundstatistiken.
 */
@RestController
@RequestMapping("/api/trades")
public class TradesRestController {

    private final TradesService tradesService;

    public TradesRestController(TradesService tradesService) {
        this.tradesService = tradesService;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String name = file.getOriginalFilename();
            if (name != null && (name.endsWith(".csv") || name.endsWith(".CSV"))) {
                tradesService.importTradesFromCsv(file);
            } else if (name != null && (name.endsWith(".xlsx") || name.endsWith(".xls"))) {
                tradesService.importTradesFromExcel(file);
            } else {
                // allow CSV or Excel
                tradesService.importTradesFromCsv(file);
            }
            resp.put("status", "ok");
        } catch (Exception e) {
            resp.put("status", "error");
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Trade> trades = tradesService.getAllTrades();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrades", trades.size());
        double sum = trades.stream().filter(t -> t.getProfitLoss() != null).mapToDouble(Trade::getProfitLoss).sum();
        double avg = trades.stream().filter(t -> t.getProfitLoss() != null).mapToDouble(Trade::getProfitLoss).average().orElse(0.0);
        stats.put("sumProfitLoss", sum);
        stats.put("avgProfitLoss", avg);
        long wins = trades.stream().filter(t -> t.getProfitLoss() != null && t.getProfitLoss() > 0).count();
        double winRate = trades.isEmpty() ? 0.0 : (wins * 100.0 / trades.size());
        stats.put("winRatePercent", winRate);
        Map<String, Long> bySymbol = trades.stream().filter(t -> t.getSymbol() != null)
                .collect(Collectors.groupingBy(Trade::getSymbol, Collectors.counting()));
        stats.put("bySymbol", bySymbol);
        return stats;
    }

    @GetMapping
    public List<Trade> getAllTrades() {
        return tradesService.getAllTrades();
    }
}

