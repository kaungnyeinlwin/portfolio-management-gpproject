package org.global.academy.dto.response;

/**
 * Data Transfer Object representing basic stock reference information.
 * 
 * Used for storing and searching the master list of available stocks
 * from NASDAQ. Contains only the essential identifying information
 * (symbol and company name) without pricing data.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class StockReference {
    /** The stock trading symbol (e.g., "AAPL") */
    public String symbol;

    /** The full company name (e.g., "Apple Inc.") */
    public String name;

    /**
     * Constructs a new StockReference.
     * 
     * @param symbol the stock symbol
     * @param name   the company name
     */
    public StockReference(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}
