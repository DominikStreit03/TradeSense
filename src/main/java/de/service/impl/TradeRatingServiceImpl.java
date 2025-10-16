package de.service.impl;

import de.model.trade.Trade;
import de.model.trade.rating.TradeLevel;
import de.model.trade.rating.TradeWinLoss;
import de.service.TradeRatingService;
import org.springframework.stereotype.Service;

/**
 * Service responsible for evaluating trades.
 * Determines Win/Loss and calculates a TradeLevel based on trade characteristics.
 * Works with the current Trade model (symbol, entryPrice, exitPrice, quantity, profitLoss, etc.).
 */
@Service
public class TradeRatingServiceImpl implements TradeRatingService {

    /**
     * Determines if a trade is a Win or a Loss.
     * Simple evaluation: profitLoss > 0 => WIN, otherwise LOSS.
     *
     * @param trade The trade to evaluate
     * @return TradeWinLoss enum representing WIN or LOSS
     */
    public TradeWinLoss determineWinLoss(Trade trade) {
        if (trade.getProfitLoss() != null && trade.getProfitLoss() > 0) {
            return TradeWinLoss.WIN;
        } else {
            return TradeWinLoss.LOSS;
        }
    }

    /**
     * Calculates the TradeLevel of a trade.
     * Logic combines several factors: profit margin, quantity, and risk estimation.
     *
     * @param trade The trade to evaluate
     * @return TradeLevel enum representing the quality of the trade
     */
    public TradeLevel calculateTradeLevel(Trade trade) {
        if (trade.getEntryPrice() == null || trade.getExitPrice() == null || trade.getQuantity() == null) {
            return TradeLevel.OKAY;
        }

        double profit = trade.getProfitLoss() != null ? trade.getProfitLoss() :
                (trade.getExitPrice() - trade.getEntryPrice()) * trade.getQuantity();

        // Negative profit = risky
        if (profit <= 0) return TradeLevel.RISKY;

        double quantityFactor = Math.log10(trade.getQuantity() + 1);
        double riskFactor = estimateRisk(trade.getEntryPrice(), trade.getExitPrice(), trade.getQuantity());

        // Weighted score
        double score = normalizeProfit(profit) * 0.6 + normalizeQuantity(quantityFactor) * 0.3 + (1 - riskFactor) * 0.1;

        // Map to 1-5
        int rating = Math.min(5, Math.max(1, (int) Math.ceil(score * 5)));

        switch (rating) {
            case 5: return TradeLevel.EXCELLENT;
            case 4: return TradeLevel.GOOD;
            case 3: return TradeLevel.OKAY;
            case 2: return TradeLevel.CAUTIOUS;
            default: return TradeLevel.RISKY;
        }
    }

    /**
     * Normalizes profit to a 0..1 scale
     *
     * @param profit trade profit
     * @return normalized value between 0 and 1
     */
    private double normalizeProfit(double profit) {
        double maxProfit = 300.0;
        return Math.min(1.0, profit / maxProfit);
    }

    /**
     * Normalizes quantity factor to 0..1
     *
     * @param quantityFactor logarithmic quantity factor
     * @return normalized value between 0 and 1
     */
    private double normalizeQuantity(double quantityFactor) {
        double maxFactor = 3.0;
        return Math.min(1.0, quantityFactor / maxFactor);
    }

    /**
     * Estimates risk based on profit and quantity.
     * Larger positions or extreme profit swings increase risk.
     *
     * @param entryPrice entry price of trade
     * @param exitPrice exit price of trade
     * @param quantity quantity of trade
     * @return risk factor between 0 (low) and 1 (high)
     */
    private double estimateRisk(Double entryPrice, Double exitPrice, Double quantity) {
        double potentialLoss = entryPrice * quantity; // max possible
        double actualLoss = Math.max(0, entryPrice - exitPrice) * quantity;
        return Math.min(1.0, actualLoss / (potentialLoss + 1)); // +1 to avoid div by zero
    }
}