package de.controller;

import de.model.Trade;
import de.service.TradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class TradesController {

    private final TradesService tradesService;

    @Autowired
    public TradesController(TradesService tradesService) {
        this.tradesService = tradesService;
    }

    @GetMapping("/trades")
    public String getTrades(Model model) {
        List<Trade> trades = tradesService.getAllTrades();
        model.addAttribute("trades", trades);
        return "trades"; // Name der View (z.B. trades.html)
    }

    @GetMapping("/trades/statistics")
    public String getTradeStatistics() {
        return "trade-statistics"; // Name der View (z.B. trade-statistics.html)
    }

    @GetMapping("/trades/overview")
    public String getTradeOverview(Model model) {
        List<Trade> trades = tradesService.getAllTrades();
        model.addAttribute("trades", trades);
        return "trade-overview"; // Name der View (z.B. trade-statistics.html)
    }

    @PostMapping("/trades")
    public String postTrade(@ModelAttribute Trade trade) {
        tradesService.saveTrade(trade);
        return "redirect:/trades";
    }

//    public String uploadTradesExcel(@RequestParam("file") MultipartFile file, Model model) {

    @PostMapping("/trades/upload")
    public String uploadTradesExcel(Model model) {
//     TODO: Re-add when import functionality is back
//        try {
//            tradesService.importTradesFromExcel(file);
//            model.addAttribute("message", "Excel-Import erfolgreich!");
//        } catch (Exception e) {
//            model.addAttribute("message", "Fehler beim Import: " + e.getMessage());
//        }
        return "redirect:/trades";
    }

}