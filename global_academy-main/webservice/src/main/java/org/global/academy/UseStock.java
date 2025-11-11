package org.global.academy;

public class UseStock {
    public static void main(String[] args) {
        Stock nvid = new Stock("NVIDIA", "NVDA", "NASDAQ", 170.0);
        System.out.println(nvid.getSymbol());
    }
}
