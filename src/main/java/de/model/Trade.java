package de.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private Double buyIn;
    private Double sellOut;
    private String symbol;
    private LocalDateTime tradeTime;

    // Standard-Konstruktor
    public Trade() {}

    public Double getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(Double buyIn) {
        this.buyIn = buyIn;
    }

    public Double getSellOut() {
        return sellOut;
    }

    public void setSellOut(Double sellOut) {
        this.sellOut = sellOut;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDateTime getTradeTime() {
        return tradeTime;
    }

    //TODO Time and Date to LacalDateTime
    public void setTradeTime(LocalDateTime tradeTime) {
        this.tradeTime = tradeTime;
    }
}

