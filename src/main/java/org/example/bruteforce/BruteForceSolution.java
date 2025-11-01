package org.example.bruteforce;

import org.example.data.Item;
import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Unit;

import java.util.*;
import java.util.stream.Collectors;

public class BruteForceSolution {
    private final MiningData miningData;
    private final double minEsupRate;

    public BruteForceSolution(MiningData miningData, double minEsupRate) {
        this.miningData = miningData;
        this.minEsupRate = minEsupRate;
    }

    public void execute() {
        List<Item> items = miningData.getItems();
        List<Transaction> transactions = miningData.getTransactions();

        double minEsup = minEsupRate * transactions.size();

        System.out.println("========== All Itemsets ==========");
        List<BFItemset> allBFItemsets = generateAllPossibleItemsets(items);
        allBFItemsets.forEach(BFItemset -> {
            double expectedSupport = 0;
            for(Transaction transaction : transactions) {
                expectedSupport += calculateExistedProbabilityForItemset(BFItemset, transaction);
            }
            BFItemset.setExpectedSupport(expectedSupport);
            System.out.println(BFItemset);
        });

        List<BFItemset> frequentBFItemsets = allBFItemsets.stream()
            .filter(i -> i.getExpectedSupport() > minEsup)
            .toList();

        List<BFItemset> maximalFrequentBFItemsets = frequentBFItemsets.stream()
            .filter(i -> isMaximalItemset(i, frequentBFItemsets))
            .toList();

        System.out.println("========== Frequent Itemsets ==========");
        frequentBFItemsets.forEach(System.out::println);

        System.out.println("========== Maximal Frequent Itemsets ==========");
        maximalFrequentBFItemsets.forEach(System.out::println);
    }

    private List<BFItemset> generateAllPossibleItemsets(List<Item> items) {
        final List<BFItemset> BFItemsets = new ArrayList<>();
        int n = items.size();

        int maxCombinations = 1 << n;

        for (int i = 1; i < maxCombinations; i++) {
            Set<Item> currentItems = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    currentItems.add(items.get(j));
                }
            }
            BFItemsets.add(new BFItemset(currentItems));
        }

        return BFItemsets;
    }

    private double calculateExistedProbabilityForItemset(BFItemset BFItemset, Transaction transaction) {
        Map<Item, Double> transactionItemProbabilities = transaction.getUnits().stream()
            .collect(Collectors.toMap(
                Unit::getItem,
                Unit::getProbability
            ));

        double expectedSupport = 1.0;

        for (Item item : BFItemset.getItems()) {
            if (transactionItemProbabilities.containsKey(item)) {
                expectedSupport *= transactionItemProbabilities.get(item);
            } else {
                return 0.0;
            }
        }

        return expectedSupport;
    }

    private boolean isMaximalItemset(BFItemset BFItemset, List<BFItemset> frequentBFItemsets) {
        Set<Item> setX = new HashSet<>(BFItemset.getItems());
        int sizeX = setX.size();

        for (BFItemset BFItemsetY : frequentBFItemsets) {
            int sizeY = BFItemsetY.getItems().size();
            if (sizeY > sizeX) {
                Set<Item> setY = new HashSet<>(BFItemsetY.getItems());
                if (setY.containsAll(setX)) {
                    return false;
                }
            }
        }
        return true;
    }
}
