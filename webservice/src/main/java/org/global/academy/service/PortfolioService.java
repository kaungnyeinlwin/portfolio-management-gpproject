package org.global.academy.service;

import org.global.academy.dto.request.BuyStockRequest;
import org.global.academy.model.Portfolio;
import org.global.academy.model.Stock;
import org.global.academy.repository.PortfolioRepository;
import org.global.academy.repository.StockRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for portfolio-related business logic.
 * 
 * Handles portfolio operations including viewing holdings,
 * buying stocks, and calculating portfolio values with current prices.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    /**
     * Constructs a new PortfolioService.
     * 
     * @param portfolioRepository the portfolio repository for data access
     * @param stockRepository     the stock repository for price data
     */
    public PortfolioService(PortfolioRepository portfolioRepository,
            StockRepository stockRepository) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Gets the portfolio data for a user with current prices.
     * 
     * @param username the username
     * @return map containing portfolio data (holdings, totalValue, totalGain)
     */
    public Map<String, Object> getPortfolioData(String username) {
        Portfolio portfolio = portfolioRepository.getPortfolio(username);

        Map<String, Map<String, Object>> aggregatedMap = portfolio.getAggregatedHoldings();
        List<Map<String, Object>> holdings = new ArrayList<>(aggregatedMap.values());

        // Get live prices
        List<String> symbols = holdings.stream()
                .map(h -> h.get("symbol").toString())
                .collect(Collectors.toList());

        Map<String, Double> currentPrices = stockRepository.fetchCurrentPrices(symbols);

        // Calculate current values and gains
        for (Map<String, Object> holding : holdings) {
            String symbol = holding.get("symbol").toString();
            double currentPrice = currentPrices.getOrDefault(symbol,
                    ((Number) holding.get("price")).doubleValue());
            double purchasePrice = ((Number) holding.get("purchasePrice")).doubleValue();
            int quantity = ((Number) holding.get("quantity")).intValue();

            double gain = (currentPrice - purchasePrice) * quantity;
            double currentValue = currentPrice * quantity;

            holding.put("currentPrice", currentPrice);
            holding.put("gain", gain);
            holding.put("currentValue", currentValue);
        }

        double totalValue = holdings.stream()
                .mapToDouble(h -> ((Number) h.get("currentValue")).doubleValue())
                .sum();

        double totalGain = holdings.stream()
                .mapToDouble(h -> ((Number) h.get("gain")).doubleValue())
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("holdings", holdings);
        result.put("totalValue", totalValue);
        result.put("totalGain", totalGain);

        return result;
    }

    /**
     * Buys stocks for a user.
     * 
     * @param username the username
     * @param request  the buy stock request
     * @return true if purchase successful
     */
    public boolean buyStock(String username, BuyStockRequest request) {
        Portfolio portfolio = portfolioRepository.getPortfolio(username);

        Stock newStock = new Stock(
                request.name,
                request.symbol,
                "US",
                request.price);

        portfolio.addStock(newStock, request.quantity);
        portfolioRepository.saveAllHoldings();

        return true;
    }

    /**
     * Gets the portfolio for a user (creates if doesn't exist).
     * 
     * @param username the username
     * @return the user's portfolio
     */
    public Portfolio getPortfolio(String username) {
        return portfolioRepository.getPortfolio(username);
    }
}
