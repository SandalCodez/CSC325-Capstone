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
    private double balance;
    private double buyPrice;
    private int totalShares;
    private Date buyDate;
    private double currentPrice;

    private final SimpleDoubleProperty totalValue = new SimpleDoubleProperty();
    private final SimpleDoubleProperty unrealizedGainLoss = new SimpleDoubleProperty();
    private final SimpleDoubleProperty profitLossPercentage = new SimpleDoubleProperty();
    private final SimpleDoubleProperty portfolioWeight = new SimpleDoubleProperty();

    public PortfolioEntry() {}

    public PortfolioEntry(String tickerSymbol, String companyName, int totalShares, double buyPrice, Date buyDate) {
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.totalShares = totalShares;
        this.buyPrice = buyPrice;
        this.buyDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static PortfolioEntry fromStock(String ticker,String companyName, int shares, double buyPrice, Date buyDate, double currentPrice) {
        PortfolioEntry entry = new PortfolioEntry();

        entry.setTickerSymbol(ticker);
        entry.setCompanyName(companyName);
        entry.setTotalShares(shares);
        entry.setBuyPrice(buyPrice);
        entry.setBuyDate(buyDate);
        entry.setCurrentPrice(currentPrice);


        // You can set portfolioWeight later after computing total portfolio value
        return entry;
    }

    public static PortfolioEntry fromMap(Map<String, Object> map) {
        PortfolioEntry entry = new PortfolioEntry();

        entry.setTickerSymbol((String) map.get("tickerSymbol"));
        entry.setCompanyName((String) map.get("companyName"));
        entry.setBuyPrice((Double) map.get("buyPrice"));
        entry.setTotalShares(((Long) map.get("totalShares")).intValue());

        Object balanceObj = map.get("balance");
        if (balanceObj instanceof Double) {
            entry.setBalance((Double) balanceObj);
        }

        Object dateObj = map.get("buyDate");
        if (dateObj instanceof com.google.cloud.Timestamp) {
            entry.setBuyDate(((com.google.cloud.Timestamp) dateObj).toDate());
        } else if (dateObj instanceof Date) {
            entry.setBuyDate((Date) dateObj);
        } else {
            entry.setBuyDate(null); // fallback
        }

        Object current = map.get("currentPrice");
        if (current instanceof Double) {
            entry.setCurrentPrice((Double) current);
        }

        Object tv = map.get("totalValue");
        if (tv instanceof Double) {
            entry.setTotalValue((Double) tv);
        }

        Object ug = map.get("unrealizedGainLoss");
        if (ug instanceof Double) {
            entry.setUnrealizedGainLoss((Double) ug);
        }

        Object pl = map.get("profitLossPercentage");
        if (pl instanceof Double) {
            entry.setProfitLossPercentage((Double) pl);
        }

        Object weight = map.get("portfolioWeight");
        if (weight instanceof Double) {
            entry.setPortfolioWeight((Double) weight);
        }

        return entry;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("tickerSymbol", tickerSymbol);
        map.put("companyName", companyName);
        map.put("totalShares", totalShares);
        map.put("buyPrice", buyPrice);
        map.put("buyDate", buyDate);
        return map;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public double getTotalValue(){
        return currentPrice * totalShares;
    }

    public void setTotalValue(double value){
        totalValue.set(value);
    }

   public double getUnrealizedGainLoss(){
        return (currentPrice - buyPrice) * totalShares;
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

    public double getProfitLossPercentage() {
        return (buyPrice == 0) ? 0 : ((currentPrice - buyPrice) / buyPrice) * 100;
    }
    public void setProfitLossPercentage(double profitLossPercentage) {
        this.profitLossPercentage.set(profitLossPercentage);
    }
    public double getPortfolioWeight() {
        return portfolioWeight.get();
    }
    public void setPortfolioWeight(double portfolioWeight) {
        this.portfolioWeight.set(portfolioWeight);
    }

}