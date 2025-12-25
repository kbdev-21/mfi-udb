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

        Map<Item, Double> filteredAndSortedItemsEsupMap = calculateFilteredAndSortedItemsEsupMap(transactions, minExpectedSupport);

        //filteredAndSortedItemsEsupMap.forEach((key, value) -> System.out.println(key.getId() + ": " + value));

        sortTransactionUnits(transactions, filteredAndSortedItemsEsupMap);

        Map<String, ItemNodeEntry> headerTable = new HashMap<>();
        UFPTreeNode root = new UFPTreeNode(null, 1.0,  null);

        /* insert transactions to tree */
        transactions.forEach(transaction -> {
            System.out.println("NEW PATH:");
            UFPTreeNode current = root;
            for(Unit u: transaction.getUnits()) {
                double roundedProbability = DiscretizationUtil.roundingWithBin(u.getProbability(), 4);
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

        /* mining frequent itemsets from tree */
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

                    // node hiện tại vừa được thêm vào path
                    path.add(node);

                    // =========================================
                    // 1) Tạo itemset 2 phần tử: {base, node mới}
                    // =========================================
                    Set<String> twoItemset = new HashSet<>();
                    twoItemset.add(baseItemId);
                    twoItemset.add(node.getItemId());

                    double esupTwo = baseProbability * node.getProbability();
                    itemsetsEsup.merge(twoItemset, esupTwo, Double::sum);

                    // Di chuyển lên parent
                    node = node.getParent();

                    // =========================================
                    // 2) Tạo itemset lớn hơn từ cả path
                    // =========================================
                    Set<String> itemsetIds = new HashSet<>();
                    itemsetIds.add(baseItemId);

                    double esup = baseProbability;
                    for (UFPTreeNode n : path) {
                        if (!n.getItemId().equals(baseItemId)) {
                            itemsetIds.add(n.getItemId());
                            esup *= n.getProbability();
                        }
                    }

                    // =========================================
                    // 3) Tránh trùng với twoItemset
                    // =========================================
                    if (!itemsetIds.equals(twoItemset)) {
                        itemsetsEsup.merge(itemsetIds, esup, Double::sum);
                    }
                }

                if (currentTail.getNeighbor() == null) {
                    break;
                }
                currentTail = currentTail.getNeighbor();
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

        return frequentItemsets;
    }

    private Map<Item, Double> calculateFilteredAndSortedItemsEsupMap(List<Transaction> transactions, double minExpectedSupport) {
        Map<Item, Double> itemsEsupMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Unit u : t.getUnits()) {
                itemsEsupMap.merge(u.getItem(), u.getProbability(), Double::sum);
            }
        }

        return itemsEsupMap.entrySet()
            .stream()
            .filter(e -> e.getValue() >= minExpectedSupport)
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new)
            );
    }

    private void sortTransactionUnits(List<Transaction> transactions, Map<Item, Double> filteredAndSortedItemsEsupMap) {
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

//            System.out.println("TRANSACTION:");
//            transaction.getUnits().forEach(unit -> {
//                System.out.println(unit.getItem().getId() + ": " + unit.getProbability());
//            });
        });
    }
}
