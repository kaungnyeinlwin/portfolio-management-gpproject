package org.global.academy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Portfolio {

    private List<Stock> holdings;

    public Portfolio() {
        this.holdings = new ArrayList<>();
    }

    public void addStock(Stock stockToAdd, int quantity) {
        for (int i = 0; i < quantity; i++) {
            this.holdings.add(stockToAdd);
        }
        System.out.printf("✅ Purchased %d share(s) of %s.%n", quantity, stockToAdd.getSymbol());
    }

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

    // M2
    public double getValue() {
        double totalValue = 0.0;
        for (Stock stock : this.holdings) {
            totalValue += stock.getPrice();
        }
        return totalValue;
    }

    public List<Stock> getHoldings() {
        return new ArrayList<>(this.holdings);
    }

    // Get aggregated holdings by symbol with quantities and profit calculation
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

    @Override
    public String toString() {
        if (this.holdings.isEmpty()) {
            return "Portfolio is empty.";
        }
        return "Portfolio containing: " + this.holdings.toString();
    }
}