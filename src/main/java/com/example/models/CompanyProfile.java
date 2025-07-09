package com.example.models;

public class CompanyProfile {
    private String name;
    private String industry;
    private double marketCap;
    private long sharesOutstanding;

    public CompanyProfile(String name, String industry, double marketCap, long sharesOutstanding) {
        this.name = name;
        this.industry = industry;
        this.marketCap = marketCap;
        this.sharesOutstanding = sharesOutstanding;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    public double getMarketCap() {
        return marketCap;
    }
    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }
    public long getSharesOutstanding() {
        return sharesOutstanding;
    }
    public void setSharesOutstanding(long sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }
}
