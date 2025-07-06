package com.example.models;

import com.google.cloud.firestore.annotation.IgnoreExtraProperties;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Transaction {

    private String id;
    private String tickerSymbol;
    private int quantity;
    private double pricePerShare;
    private boolean isBuy;
    private LocalDateTime timestamp;

    public Transaction() {
    }

    public Transaction(String id, String tickerSymbol, int quantity, double pricePerShare, Boolean isBuy, LocalDateTime timestamp) {
        this.id = id;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.isBuy = isBuy;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("tickerSymbol", tickerSymbol);
        map.put("quantity", quantity);
        map.put("pricePerShare", pricePerShare);
        map.put("isBuy", isBuy);
        map.put("timestamp", timestamp);
        return map;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }
    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(double pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public boolean getIsBuy() {
        return isBuy;
    }
    public void setIsBuy(boolean isBuy) {
        this.isBuy = isBuy;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
