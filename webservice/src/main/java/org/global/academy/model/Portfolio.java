package org.global.academy.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a user's stock portfolio in the portfolio management system.
 * 
 * This class manages a collection of stock holdings, allowing users to:
 * 
 * Add stocks to their portfolio (buy)
 * - Remove stocks from their portfolio (sell - future implementation)
 * - Calculate total portfolio value
 * - View aggregated holdings by stock symbol
 * 
 * Each stock is stored individually to maintain purchase history and enable
 * accurate profit/loss tracking.
 * 
 * 
 * @author Project Team 5
 * @version 1.0
 */
public class Portfolio {

    /**
     * List of all stock holdings in this portfolio.
     * Each stock instance represents one share, allowing for detailed tracking
     * of individual purchases at different prices.
     */
    private List<Stock> holdings;

    /**
     * Constructs a new empty Portfolio.
     * Initializes the holdings list to store future stock purchases.
     */
    public Portfolio() {
        this.holdings = new ArrayList<>();
    }

    /**
     * Adds a specified quantity of a stock to this portfolio.
     * 
     * This method simulates purchasing stocks by adding the same stock
     * instance multiple times (once per share). A confirmation message
     * is printed to the console upon successful purchase.
     * 
     * @param stockToAdd the stock to add to the portfolio
     * @param quantity   the number of shares to purchase (must be positive)
     */
    public void addStock(Stock stockToAdd, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.holdings.add(stockToAdd);
        }
        System.out.printf("✅ Purchased %d share(s) of %s.%n", quantity, stockToAdd.getSymbol());
    }

    /**
     * Removes a specified quantity of a stock from this portfolio.
     * 
     * This method simulates selling stocks. It first verifies that the user
     * owns enough shares before proceeding with the sale. If insufficient
     * shares are available, an error message is displayed and no stocks are
     * removed.
     * 
     * @param tickerSymbol the stock symbol to sell (e.g., "AAPL")
     * @param quantity     the number of shares to sell
     */
    public void removeStock(String tickerSymbol, int quantity) {
        long ownedCount = this.holdings.stream()
                .filter(s -> s.getSymbol().equals(tickerSymbol))
                .count();

        if (quantity > ownedCount) {
            System.out.printf("⚠️ Error: Cannot sell %d share(s) of %s. You only own %d.%n",
                    quantity, tickerSymbol, ownedCount);
            return;
        }

        int removedCount = 0;
        Iterator<Stock> iterator = this.holdings.iterator();
        while (iterator.hasNext() && removedCount < quantity) {
            Stock currentStock = iterator.next();
            if (currentStock.getSymbol().equals(tickerSymbol)) {
                iterator.remove();
                removedCount++;
            }
        }
        System.out.printf("🔻 Sold %d share(s) of %s.%n", quantity, tickerSymbol);
    }

    /**
     * Calculates the total current value of this portfolio.
     * 
     * The value is computed by summing the current market price of all
     * stocks held in the portfolio.
     * 
     * @return the total portfolio value based on current stock prices
     */
    public double getValue() {
        double totalValue = 0.0;
        for (Stock stock : this.holdings) {
            totalValue += stock.getPrice();
        }
        return totalValue;
    }

    /**
     * Gets a copy of all stock holdings in this portfolio.
     * 
     * Returns a new ArrayList to prevent external modification of the
     * internal holdings list.
     * 
     * @return a list containing all stocks in this portfolio
     */
    public List<Stock> getHoldings() {
        return new ArrayList<>(this.holdings);
    }

    /**
     * Gets aggregated holdings grouped by stock symbol.
     * 
     * This method consolidates multiple shares of the same stock into a single
     * entry, providing:
     * 
     * - Company name and symbol
     * - Total quantity of shares
     * - Average price per share
     * - Purchase price information
     * - Total current value and purchase value
     * 
     * For displaying portfolio summaries and calculating profit/loss per stock.
     * 
     * @return a map where keys are stock symbols and values are maps containing
     *         aggregated data (name, symbol, quantity, price, purchasePrice,
     *         totalPrice, totalPurchasePrice)
     */
    public Map<String, Map<String, Object>> getAggregatedHoldings() {
        Map<String, Map<String, Object>> aggregated = new HashMap<>();

        for (Stock stock : this.holdings) {
            String symbol = stock.getSymbol();

            if (aggregated.containsKey(symbol)) {
                Map<String, Object> data = aggregated.get(symbol);
                int quantity = (Integer) data.get("quantity");
                double totalCurrentPrice = (Double) data.get("totalPrice");
                double totalPurchasePrice = (Double) data.get("totalPurchasePrice");

                data.put("quantity", quantity + 1);
                data.put("totalPrice", totalCurrentPrice + stock.getPrice());
                data.put("totalPurchasePrice", totalPurchasePrice + stock.getPurchasePrice());
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("name", stock.getCompanyName());
                data.put("symbol", symbol);
                data.put("quantity", 1);
                data.put("price", stock.getPrice());
                data.put("purchasePrice", stock.getPurchasePrice());
                data.put("totalPrice", stock.getPrice());
                data.put("totalPurchasePrice", stock.getPurchasePrice());

                aggregated.put(symbol, data);
            }
        }

        return aggregated;
    }

    /**
     * Returns a string representation of this portfolio.
     * 
     * If the portfolio is empty, returns a message indicating so.
     * Otherwise, returns a description including all holdings.
     * 
     * @return a string describing the portfolio contents
     */
    @Override
    public String toString() {
        if (this.holdings.isEmpty()) {
            return "Portfolio is empty.";
        }
        return "Portfolio containing: " + this.holdings.toString();
    }
}