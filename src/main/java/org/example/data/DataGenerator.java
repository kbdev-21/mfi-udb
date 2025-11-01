package org.example.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {
    public static MiningData generateRandomData(int itemsCount, int transactionsCount) {
        List<Item> allItems = generateRandomItems(itemsCount);
        List<Transaction> allTransactions = generateRandomTransactions(allItems, transactionsCount, 1, itemsCount);
        return new MiningData(allItems, allTransactions);
    }

    private static List<Item> generateRandomItems(int count) {
        List<Item> allItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String itemName = generateItemName(i, count);
            allItems.add(new Item(itemName));
        }
        return allItems;
    }

    private static String generateItemName(int index, int totalCount) {
        int paddingLength = (int) Math.ceil(Math.log10(totalCount + 1));
        if (paddingLength < 1) {
            paddingLength = 1;
        }
        String formatSpecifier = String.format("I%%0%dd", paddingLength);
        return String.format(formatSpecifier, index + 1);
    }

    private static List<Transaction> generateRandomTransactions(List<Item> allItems, int count, int minItems, int maxItems) {
        List<Transaction> generatedTransactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            List<Unit> units = new ArrayList<>();
            int numItems = random.nextInt(maxItems - minItems + 1) + minItems;

            for (int j = 0; j < numItems; j++) {
                Item randomItem = allItems.get(random.nextInt(allItems.size()));
                if (units.stream().noneMatch(u -> u.getItem().equals(randomItem))) {
                    double prob = 0.1 + (1.0 - 0.1) * random.nextDouble();
                    units.add(new Unit(randomItem, prob));
                }
            }
            generatedTransactions.add(new Transaction(units));
        }
        return generatedTransactions;
    }
}