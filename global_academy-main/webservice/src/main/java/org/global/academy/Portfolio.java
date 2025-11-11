package org.global.academy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    @Override
    public String toString() {
        if (this.holdings.isEmpty()) {
            return "Portfolio is empty.";
        }
        return "Portfolio containing: " + this.holdings.toString();
    }
}