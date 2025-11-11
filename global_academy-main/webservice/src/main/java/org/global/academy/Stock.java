package org.global.academy;

public class Stock {

    String companyName;
    String companySymbol;
    String stockExchange;
    double stockPrice;

    public Stock(String name, String symbol, String exchange, double price) {
        companyName = name;
        companySymbol = symbol;
        stockExchange = exchange;
        stockPrice = price;
    }

    public String getSymbol() {
        return companySymbol;
    }

    public double getPrice() {
        return this.stockPrice;
    }
}
