package de.repository;

import de.model.trade.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    // CRUD-Methoden über JpaRepository verfügbar.
    // weitere querry methoden können hier definiert werden
}