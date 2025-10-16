package de.service;

import de.model.trade.Trade;
import de.model.trade.rating.TradeLevel;
import de.model.trade.rating.TradeWinLoss;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

@Component
public interface TradeRatingService {
    /**
     * Determines if a trade is a Win or a Loss.
     *
     * @param trade The trade to evaluate
     * @return TradeWinLoss enum representing WIN or LOSS
     */
    TradeWinLoss determineWinLoss(Trade trade);

    /**
     * Calculates the TradeLevel of a trade.
     * The logic considers multiple factors: profit, quantity, and estimated risk.
     *
     * @param trade The trade to evaluate
     * @return TradeLevel enum representing the quality of the trade
     */
    TradeLevel calculateTradeLevel(Trade trade);
}
