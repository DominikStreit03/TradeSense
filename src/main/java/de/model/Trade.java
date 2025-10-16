package de.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "trades")
public class Trade {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    private Double entryPrice;

    private Double exitPrice;

    private Double quantity;

    private Double profitLoss;

    private LocalDateTime timestamp;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_tags", joinColumns = @JoinColumn(name = "trade_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public Trade() {
    }

    // Convenience constructor
    public Trade(String symbol, Double entryPrice, Double exitPrice, Double quantity, Double profitLoss, LocalDateTime timestamp, Set<String> tags, String notes) {
        this.symbol = symbol;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.quantity = quantity;
        this.profitLoss = profitLoss;
        this.timestamp = timestamp;
        if (tags != null) {
            this.tags = tags;
        }
        this.notes = notes;
    }

    // Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public Double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(Double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getProfitLoss() {
        if (entryPrice == null || exitPrice == null || quantity == null) {
            return 0.0;
        }
        return (exitPrice - entryPrice) * quantity;
    }

    public void setProfitLoss(Double exitPrice, Double entryPrice, Double quantity) {
        if (exitPrice == null || entryPrice == null || quantity == null) {
            this.profitLoss = 0.0;
        } else {
            this.profitLoss = (exitPrice - entryPrice) * quantity;
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? tags : new HashSet<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // equals / hashCode based on id when available, otherwise on business key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trade trade = (Trade) o;

        return Objects.equals(id, trade.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", entryPrice=" + entryPrice +
                ", exitPrice=" + exitPrice +
                ", quantity=" + quantity +
                ", profitLoss=" + profitLoss +
                ", timestamp=" + timestamp +
                ", tags=" + tags +
                ", notes='" + (notes != null ? notes.replaceAll("\\s+", " ").trim() : null) + '\'' +
                '}';
    }
}

