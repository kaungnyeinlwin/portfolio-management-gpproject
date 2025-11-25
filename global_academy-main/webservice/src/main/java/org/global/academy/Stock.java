package org.global.academy;

public class Stock {

    String companyName;
    String companySymbol;
    String stockExchange;
    double stockPrice;
    double purchasePrice;

    public Stock(String name, String symbol, String exchange, double price) {
        companyName = name;
        companySymbol = symbol;
        stockExchange = exchange;
        stockPrice = price;
        this.purchasePrice = price; // Set purchase price to initial price
    }

    public double getPurchasePrice() {
        return this.purchasePrice;
    }

    public void setPurchasePrice(double price) {
        this.purchasePrice = price;
    }

    public String getSymbol() {
        return companySymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getPrice() {
        return this.stockPrice;
    }

    public void setPrice(double price) {
        this.stockPrice = price;
    }

    @Override
    public String toString() {
        return companyName + " (" + companySymbol + ")";
    }
}
