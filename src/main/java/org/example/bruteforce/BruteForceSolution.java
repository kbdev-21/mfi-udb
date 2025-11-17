package org.example.bruteforce;

import org.example.data.*;

import java.util.*;
import java.util.stream.Collectors;

public class BruteForceSolution {
    private final MiningData miningData;
    private final double minEsupRate;

    public BruteForceSolution(MiningData miningData, double minEsupRate) {
        this.miningData = miningData;
        this.minEsupRate = minEsupRate;
    }

    public List<Itemset> maximalFrequentItemsets() {
        List<Item> items = miningData.getItems();
        List<Transaction> transactions = miningData.getTransactions();

        double minExpectedSupport = minEsupRate * transactions.size();

        List<Itemset> allItemsets = generateAllPossibleItemsets(items);
        allItemsets.forEach(Itemset -> {
            double expectedSupport = 0;
            for(Transaction transaction : transactions) {
                expectedSupport += calculateExistedProbabilityForItemset(Itemset, transaction);
            }
            Itemset.setExpectedSupport(expectedSupport);
        });

        List<Itemset> frequentItemsets = allItemsets.stream()
            .filter(i -> i.getExpectedSupport() > minExpectedSupport)
            .toList();

//        return frequentItemsets.stream()
//            .filter(i -> isMaximalItemset(i, frequentItemsets))
//            .toList();
        return frequentItemsets;
    }

    private List<Itemset> generateAllPossibleItemsets(List<Item> items) {
        final List<Itemset> Itemsets = new ArrayList<>();
        int n = items.size();

        int maxCombinations = 1 << n;

        for (int i = 1; i < maxCombinations; i++) {
            Set<Item> currentItems = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    currentItems.add(items.get(j));
                }
            }
            Itemsets.add(new Itemset(currentItems));
        }

        return Itemsets;
    }

    private double calculateExistedProbabilityForItemset(Itemset Itemset, Transaction transaction) {
        Map<Item, Double> transactionItemProbabilities = transaction.getUnits().stream()
            .collect(Collectors.toMap(
                Unit::getItem,
                Unit::getProbability
            ));

        double expectedSupport = 1.0;

        for (Item item : Itemset.getItems()) {
            if (transactionItemProbabilities.containsKey(item)) {
                expectedSupport *= transactionItemProbabilities.get(item);
            } else {
                return 0.0;
            }
        }

        return expectedSupport;
    }

    private boolean isMaximalItemset(Itemset Itemset, List<Itemset> frequentItemsets) {
        Set<Item> setX = new HashSet<>(Itemset.getItems());
        int sizeX = setX.size();

        for (Itemset ItemsetY : frequentItemsets) {
            int sizeY = ItemsetY.getItems().size();
            if (sizeY > sizeX) {
                Set<Item> setY = new HashSet<>(ItemsetY.getItems());
                if (setY.containsAll(setX)) {
                    return false;
                }
            }
        }
        return true;
    }
}
