package org.global.academy.service;

import org.global.academy.dto.response.StockReference;
import org.global.academy.repository.StockRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for stock-related business logic.
 * 
 * Handles stock search, price fetching, and stock data management.
 * Provides a clean interface for controllers to access stock information.
 * 
 * @author Project Group 5
 * @version 1.0
 */
public class StockService {

    private final StockRepository stockRepository;

    /**
     * Constructs a new StockService.
     * 
     * @param stockRepository the stock repository for data access
     */
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Searches for stocks matching the query.
     * 
     * @param query the search query (can be null for default stocks)
     * @return list of maps containing stock data with prices
     */
    public List<Map<String, Object>> searchStocks(String query) {
        List<StockReference> results = stockRepository.searchStocks(query, 20);

        List<String> symbols = results.stream()
                .map(s -> s.symbol)
                .collect(Collectors.toList());

        Map<String, Double> prices = stockRepository.fetchCurrentPrices(symbols);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (StockReference stock : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("symbol", stock.symbol);
            map.put("name", stock.name);

            Double price = prices.get(stock.symbol);
            if (price != null) {
                map.put("price", price);
            } else {
                map.put("price", "Unavailable");
            }

            responseList.add(map);
        }

        return responseList;
    }

    /**
     * Gets current prices for a list of stock symbols.
     * 
     * @param symbols list of stock symbols
     * @return map of symbols to prices
     */
    public Map<String, Double> getCurrentPrices(List<String> symbols) {
        return stockRepository.fetchCurrentPrices(symbols);
    }

    /**
     * Gets all available stocks.
     * 
     * @return list of all stock references
     */
    public List<StockReference> getAllStocks() {
        return stockRepository.getAllStocks();
    }
}
