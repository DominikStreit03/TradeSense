package de.model.trade.rating;

public enum TradeWinLoss {
    WIN("🟢", "Profit trade"),
    LOSS("🔴", "Loss trade");

    private final String icon;
    private final String description;

    TradeWinLoss(String icon, String description) {
        this.icon = icon;
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}