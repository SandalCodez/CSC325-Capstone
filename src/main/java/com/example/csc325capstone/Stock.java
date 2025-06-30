package com.example.csc325capstone;

public class Stock {

    private String tickerSymbol;
    private String companyName;
    private double currentPrice;
    private double percentChange;

    public Stock(String tickerSymbol, String companyName, double currentPrice, double percentChange) {
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.percentChange = percentChange;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(double percentChange) {
        this.percentChange = percentChange;
    }
}
