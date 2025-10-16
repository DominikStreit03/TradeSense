package de.service.impl;

import de.model.trade.Trade;
import de.model.trade.rating.TradeLevel;
import de.model.trade.rating.TradeWinLoss;
import de.service.TradeRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TradeRatingServiceImplTest {
    private TradeRatingService tradeRatingServiceImpl;

    @BeforeEach
    void setUp() {
        tradeRatingServiceImpl = new TradeRatingServiceImpl();
    }

    @Test
    void testDetermineWinLoss_Win() {
        Trade trade = new Trade();
        trade.setSymbol("AAPL");
        trade.setEntryPrice(100.0);
        trade.setExitPrice(120.0);
        trade.setQuantity(10.0);

        TradeWinLoss result = tradeRatingServiceImpl.determineWinLoss(trade);

        assertEquals(TradeWinLoss.WIN, result, "Trade should be classified as WIN");
    }

    @Test
    void testDetermineWinLoss_Loss() {
        Trade trade = new Trade();
        trade.setSymbol("AAPL");
        trade.setEntryPrice(120.0);
        trade.setExitPrice(100.0);
        trade.setQuantity(10.0);

        TradeWinLoss result = tradeRatingServiceImpl.determineWinLoss(trade);

        assertEquals(TradeWinLoss.LOSS, result, "Trade should be classified as LOSS");
    }
    @Test
    void testCalculateTradeLevel_Excellent() {
        Trade trade = new Trade();
        trade.setEntryPrice(100.0);
        trade.setExitPrice(700.0); // Profit 600 -> high
        trade.setQuantity(20.0);   // Quantity factor > log10(20+1) ~ 1.32

        TradeLevel level = tradeRatingServiceImpl.calculateTradeLevel(trade);
        assertEquals(TradeLevel.EXCELLENT, level, "Trade should be EXCELLENT based on profit and quantity");
    }

    @Test
    void testCalculateTradeLevel_Good() {
        Trade trade = new Trade();
        trade.setEntryPrice(100.0);
        trade.setExitPrice(250.0); // Profit 150 -> moderate
        trade.setQuantity(10.0);   // Quantity factor ~ 1.04

        TradeLevel level = tradeRatingServiceImpl.calculateTradeLevel(trade);
        assertEquals(TradeLevel.GOOD, level, "Trade should be GOOD based on profitRatio and quantity");
    }

    @Test
    void testCalculateTradeLevel_Okay() {
        Trade trade = new Trade();
        trade.setEntryPrice(100.0);
        trade.setExitPrice(120.0); // Profit 20 -> small
        trade.setQuantity(5.0);    // Quantity factor ~ 0.78

        TradeLevel level = tradeRatingServiceImpl.calculateTradeLevel(trade);
        assertEquals(TradeLevel.OKAY, level, "Trade should be OKAY based on profitRatio");
    }

    @Test
    void testCalculateTradeLevel_Cautious() {
        Trade trade = new Trade();
        trade.setEntryPrice(100.0);
        trade.setExitPrice(105.0); // Profit 5 -> minimal
        trade.setQuantity(2.0);    // Quantity factor ~0.48

        TradeLevel level = tradeRatingServiceImpl.calculateTradeLevel(trade);
        assertEquals(TradeLevel.CAUTIOUS, level, "Trade should be CAUTIOUS for small profit");
    }

    @Test
    void testCalculateTradeLevel_Risky() {
        Trade trade = new Trade();
        trade.setEntryPrice(100.0);
        trade.setExitPrice(80.0); // Loss -20
        trade.setQuantity(5.0);

        TradeLevel level = tradeRatingServiceImpl.calculateTradeLevel(trade);
        assertEquals(TradeLevel.RISKY, level, "Trade should be RISKY for negative profit");
    }

}
