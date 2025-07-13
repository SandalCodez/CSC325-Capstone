package com.example.models;

import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import javafx.beans.property.SimpleDoubleProperty;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class PortfolioEntry {

    private String tickerSymbol;
    private String companyName;
    private double buyPrice;
    private int totalShares;
    private Date buyDate;
    private double currentPrice;
    private final SimpleDoubleProperty totalValue = new SimpleDoubleProperty();
    private final SimpleDoubleProperty unrealizedGainLoss = new SimpleDoubleProperty();

    public PortfolioEntry() {}

    public PortfolioEntry(String tickerSymbol, int totalShares, double buyPrice, Date buyDate) {
        this.tickerSymbol = tickerSymbol;
        this.buyPrice = buyPrice;
        this.totalShares = totalShares;
        this.buyDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static PortfolioEntry fromStock(Stock stock, int totalShares, double buyPrice) {
        PortfolioEntry entry = new PortfolioEntry(
                stock.getTickerSymbol(),
                totalShares,
                buyPrice,
                Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        double currentPrice = stock.getCurrentPrice();

        entry.setCurrentPrice(stock.getCurrentPrice());
        entry.setTotalValue(currentPrice * totalShares);
        entry.setUnrealizedGainLoss((currentPrice - buyPrice) * totalShares);

        return entry;
    }

    public static PortfolioEntry fromMap(Map<String, Object> map) {
        PortfolioEntry entry = new PortfolioEntry();

        entry.setTickerSymbol((String) map.get("tickerSymbol"));
        entry.setTotalShares(((Long) map.get("totalShares")).intValue());  // Firestore returns Long
        entry.setBuyPrice((Double) map.get("buyPrice"));

        Object dateObj = map.get("buyDate");
        if (dateObj instanceof com.google.cloud.Timestamp) {
            entry.setBuyDate(((com.google.cloud.Timestamp) dateObj).toDate());
        } else if (dateObj instanceof Date) {
            entry.setBuyDate((Date) dateObj);
        } else {
            entry.setBuyDate(null); // or handle default
        }

        return entry;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("tickerSymbol", tickerSymbol);
        map.put("totalShares", totalShares);
        map.put("buyPrice", buyPrice);
        map.put("buyDate", buyDate);
        return map;
    }

    public double getTotalValue(){
        return totalValue.get();
    }

    public void setTotalValue(double value){
        totalValue.set(value);
    }

   public double getUnrealizedGainLoss(){
        return unrealizedGainLoss.get();
   }

    public void setUnrealizedGainLoss(double value){
        unrealizedGainLoss.set(value);
    }

    public String getCompanyName(){
        return companyName;
    }

    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }


    public String getTickerSymbol() {
        return tickerSymbol;
    }
    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Date getBuyDate() {
        return buyDate;
    }

    public void setBuyDate(Date buyDate) {
        this.buyDate = buyDate;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }


}