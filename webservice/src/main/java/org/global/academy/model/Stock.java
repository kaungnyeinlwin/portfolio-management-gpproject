package org.global.academy.model;

/**
 * Represents a stock (equity) in the portfolio management system.
 * 
 * This class encapsulates all the information about a single stock,
 * including its company details, trading symbol, exchange, and pricing
 * information.
 * Both current price and purchase price are tracked to enable profit/loss
 * calculations.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class Stock {

    /** The full name of the company that issued this stock */
    String companyName;

    /** The trading symbol/ticker for this stock (e.g., "AAPL" for Apple Inc.) */
    String companySymbol;

    /** The stock exchange where this stock is traded (e.g., "NASDAQ", "NYSE") */
    String stockExchange;

    /** The current market price of the stock */
    double stockPrice;

    /**
     * The price at which this stock was purchased (used for profit/loss
     * calculation)
     */
    double purchasePrice;

    /**
     * Constructs a new Stock with the specified details.
     * 
     * The purchase price is automatically set to the initial price provided.
     * This ensures that newly created stocks have a baseline for profit/loss
     * calculations.
     * 
     * @param name     the full company name
     * @param symbol   the trading symbol/ticker
     * @param exchange the stock exchange where this stock is traded
     * @param price    the initial/current price of the stock
     */
    public Stock(String name, String symbol, String exchange, double price) {
        companyName = name;
        companySymbol = symbol;
        stockExchange = exchange;
        stockPrice = price;
        this.purchasePrice = price; // Set purchase price to initial price
    }

    /**
     * Gets the purchase price of this stock.
     * <p>
     * The purchase price represents the price at which this stock was bought,
     * and is used to calculate profit or loss.
     * </p>
     * 
     * @return the purchase price
     */
    public double getPurchasePrice() {
        return this.purchasePrice;
    }

    /**
     * Sets the purchase price of this stock.
     * <p>
     * This method allows updating the purchase price, which may be useful
     * when loading historical data or adjusting for stock splits.
     * </p>
     * 
     * @param price the new purchase price to set
     */
    public void setPurchasePrice(double price) {
        this.purchasePrice = price;
    }

    /**
     * Gets the trading symbol of this stock.
     * 
     * @return the stock symbol/ticker (e.g., "AAPL", "MSFT")
     */
    public String getSymbol() {
        return companySymbol;
    }

    /**
     * Gets the company name of this stock.
     * 
     * @return the full company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Gets the current market price of this stock.
     * 
     * @return the current stock price
     */
    public double getPrice() {
        return this.stockPrice;
    }

    /**
     * Sets the current market price of this stock.
     * 
     * This method is typically used to update the stock price with
     * real-time or latest market data.
     * 
     * @param price the new current price to set
     */
    public void setPrice(double price) {
        this.stockPrice = price;
    }

    /**
     * Returns a string representation of this stock.
     * 
     * The format is: "Company Name (SYMBOL)"
     * 
     * @return a formatted string showing the company name and symbol
     */
    @Override
    public String toString() {
        return companyName + " (" + companySymbol + ")";
    }
}
