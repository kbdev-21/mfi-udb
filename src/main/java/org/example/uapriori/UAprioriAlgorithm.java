package org.example.uapriori;

import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Unit;

import java.util.*;

public class UAprioriAlgorithm {
    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UAprioriAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }

    public List<Set<String>> findFrequentItemsets() {
        List<Transaction> transactionsOrigin = miningData.getTransactions();

        List<Set<String>> transactions = convertToItemsetList(transactionsOrigin);
        int minSupport = (int) Math.ceil(minExpectedSupportRate * transactions.size());

        List<Set<String>> frequentItemsets = new ArrayList<>();
        Map<Set<String>, Integer> currentFrequent = findFrequentOneItemsets(transactions, minSupport);

        while (!currentFrequent.isEmpty()) {
            // Thêm tập mục phổ biến vào kết quả
            frequentItemsets.addAll(currentFrequent.keySet());
            // Sinh ứng viên mới
            Set<Set<String>> candidates = aprioriGen(currentFrequent.keySet());
            // Đếm support
            Map<Set<String>, Integer> candidateCount = countCandidates(transactions, candidates);
            // Lọc theo minSupport
            currentFrequent = filterFrequent(candidateCount, minSupport);
        }
        return frequentItemsets;
    }

    public List<Set<String>> convertToItemsetList(List<Transaction> transactions) {
        List<Set<String>> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Set<String> items = new HashSet<>();
            for (Unit unit : transaction.getUnits()) {
                items.add(unit.getItem().getId()); // lấy id của Item
            }
            result.add(items);
        }
        return result;
    }

    private Map<Set<String>, Integer> findFrequentOneItemsets(List<Set<String>> transactions, int minSupport) {
        Map<String, Integer> counts = new HashMap<>();
        for (Set<String> transaction : transactions) {
            for (String item : transaction) {
                counts.put(item, counts.getOrDefault(item, 0) + 1);
            }
        }
        Map<Set<String>, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= minSupport) {
                result.put(Set.of(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private Set<Set<String>> aprioriGen(Set<Set<String>> prevFrequent) {
        Set<Set<String>> candidates = new HashSet<>();
        List<Set<String>> list = new ArrayList<>(prevFrequent);
        int k = list.get(0).size() + 1;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Set<String> union = new HashSet<>(list.get(i));
                union.addAll(list.get(j));
                if (union.size() == k) {
                    candidates.add(union);
                }
            }
        }
        return candidates;
    }

    private Map<Set<String>, Integer> countCandidates(List<Set<String>> transactions, Set<Set<String>> candidates) {
        Map<Set<String>, Integer> counts = new HashMap<>();
        for (Set<String> transaction : transactions) {
            for (Set<String> candidate : candidates) {
                if (transaction.containsAll(candidate)) {
                    counts.put(candidate, counts.getOrDefault(candidate, 0) + 1);
                }
            }
        }
        return counts;
    }

    private Map<Set<String>, Integer> filterFrequent(Map<Set<String>, Integer> candidateCounts, int minSupport) {
        Map<Set<String>, Integer> result = new HashMap<>();
        for (Map.Entry<Set<String>, Integer> entry : candidateCounts.entrySet()) {
            if (entry.getValue() >= minSupport) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
