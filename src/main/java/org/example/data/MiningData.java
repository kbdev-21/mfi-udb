package org.example.data;

import java.util.List;

public class MiningData {
    private final List<Item> items;
    private final List<Transaction> transactions;

    public MiningData(List<Item> items, List<Transaction> transactions) {
        this.items = items;
        this.transactions = transactions;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
