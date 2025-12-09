package org.global.academy.dto.request;

/**
 * Data Transfer Object for stock purchase requests.
 * 
 * Represents the data sent from the client when buying stocks.
 * Deserialized from JSON request body.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class BuyStockRequest {
    /** The company name of the stock to purchase */
    public String name;

    /** The stock symbol to purchase */
    public String symbol;

    /** The price per share at time of purchase */
    public double price;

    /** The number of shares to purchase */
    public int quantity;
}
