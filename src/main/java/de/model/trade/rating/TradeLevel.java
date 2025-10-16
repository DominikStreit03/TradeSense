package de.model.trade.rating;

/**
 * Represents the quality level of a trade.
 * Includes a rating (1-5) and an icon for UI purposes.
 */
public enum TradeLevel {
    EXCELLENT(5, "Top tier trade, high confidence", "🔼"),
    GOOD(4, "Good trade, profitable and reasonable risk", "✅"),
    OKAY(3, "Average trade, small gain/loss", "➖"),
    CAUTIOUS(2, "Trade with higher risk or small loss, be careful", "⚠️"),
    RISKY(1, "High-risk trade, likely loss", "❌");

    private final int rating;      // 1-5 scale
    private final String description;
    private final String icon;

    TradeLevel(int rating, String description, String icon) {
        this.rating = rating;
        this.description = description;
        this.icon = icon;
    }

    public int getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}