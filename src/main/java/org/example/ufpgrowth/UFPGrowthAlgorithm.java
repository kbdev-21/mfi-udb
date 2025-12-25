package org.example.ufpgrowth;

import org.example.data.*;
import org.example.ufpgrowth.entity.ItemNodeEntry;
import org.example.ufpgrowth.entity.UFPTreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class UFPGrowthAlgorithm {
    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UFPGrowthAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }

    public List<Itemset> maximalFrequentItemsets() {
        List<Transaction> transactions = miningData.getTransactions();
        double minExpectedSupport = minExpectedSupportRate * transactions.size();

        Map<Item, Double> itemsEsupMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Unit u : t.getUnits()) {
                itemsEsupMap.merge(u.getItem(), u.getProbability(), Double::sum);
            }
        }

        Map<Item, Double> filteredAndSortedItemsEsupMap = itemsEsupMap.entrySet()
            .stream()
            .filter(e -> e.getValue() >= minExpectedSupport)
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new)
            );

        filteredAndSortedItemsEsupMap.forEach((key, value) -> System.out.println(key.getId() + ": " + value));

        transactions.forEach(transaction -> {
            List<Unit> filteredAndSortedUnits = transaction.getUnits().stream()
                .filter(u -> filteredAndSortedItemsEsupMap.containsKey(u.getItem()))
                .sorted((u1, u2) -> {
                    double es1 = filteredAndSortedItemsEsupMap.getOrDefault(u1.getItem(), 0.0);
                    double es2 = filteredAndSortedItemsEsupMap.getOrDefault(u2.getItem(), 0.0);
                    return Double.compare(es2, es1);
                })
                .collect(Collectors.toList());
            transaction.setUnits(filteredAndSortedUnits);

            System.out.println("TRANSACTION:");
            transaction.getUnits().forEach(unit -> {
                System.out.println(unit.getItem().getId() + ": " + unit.getProbability());
            });
        });

        Map<String, ItemNodeEntry> headerTable = new HashMap<>();
        UFPTreeNode root = new UFPTreeNode(null, 1.0,  null);

        transactions.forEach(transaction -> {
            System.out.println("NEW PATH:");
            UFPTreeNode current = root;
            for(Unit u: transaction.getUnits()) {
                double roundedProbability = DiscretizationUtil.roundingWithBin(u.getProbability(), 20);
                String key = u.getItem().getId() + ":" + roundedProbability;
                if(current.getChildren().containsKey(key)) {
                    current = current.getChildren().get(key);
                    current.incrementAppearance();
                }
                else {
                    current = current.addChild(u.getItem().getId(), roundedProbability);

                    String itemId = u.getItem().getId();
                    ItemNodeEntry entry = headerTable.get(itemId);

                    if (entry == null) {
                        entry = new ItemNodeEntry(current, current);
                        headerTable.put(itemId, entry);
                    } else {
                        entry.getTail().setNeighbor(current);
                        entry.setTail(current);
                    }

                }
                System.out.println("ADD NODE: " + current.getItemId() + ":" + current.getProbability() + " (" + current.getAppearance() + ")");
            }
        });

        List<String> miningItemIds = filteredAndSortedItemsEsupMap.keySet().stream()
            .map(Item::getId)
            .toList()
            .reversed();

        List<Itemset> frequentItemsets = new ArrayList<>();

        for(String itemId: miningItemIds) {
            System.out.println(itemId);

            UFPTreeNode currentTail = headerTable.get(itemId).getHead();
            Map<Set<String>, Double> itemsetsEsup = new HashMap<>();

            while(true) {
                UFPTreeNode node = currentTail;
                String baseItemId = node.getItemId();
                double baseProbability = node.getProbability() * node.getAppearance();
                List<UFPTreeNode> path = new ArrayList<>();

                while (node != null && node.getParent() != null) {
                    path.add(node);
                    node = node.getParent();

                    Set<String> itemsetIds = new HashSet<>();
                    itemsetIds.add(baseItemId);
                    double esup = baseProbability;
                    for(UFPTreeNode n: path) {
                        if(n.getItemId().equals(baseItemId)) {
                            continue;
                        }
                        itemsetIds.add(n.getItemId());
                        esup *= n.getProbability();
                    }
                    itemsetsEsup.merge(itemsetIds, esup, Double::sum);
                }

                if(path.size() <= 1) {
                    if(currentTail.getNeighbor() != null) {
                        currentTail = currentTail.getNeighbor();
                        continue;
                    }
                    else {
                        break;
                    }
                }

                if(currentTail.getNeighbor() != null) {
                    currentTail = currentTail.getNeighbor();
                }
                else {
                    break;
                }
            }

            List<Itemset> generatedFrequentItemsets = itemsetsEsup.entrySet().stream()
                .filter(entry -> entry.getValue() > minExpectedSupport)
                .map(entry -> {
                    return new Itemset(
                        entry.getKey().stream().map(id -> new Item(id)).collect(Collectors.toSet()),
                        entry.getValue()
                    );
                })
                .toList();

            frequentItemsets.addAll(generatedFrequentItemsets);
        }

        frequentItemsets.forEach(System.out::println);

        return null;
    }
}
