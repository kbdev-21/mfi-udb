package org.example.uapriori;

import org.example.data.*;

import java.util.*;

public class UAprioriAlgorithm {

    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UAprioriAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }

    public List<Itemset> findFrequentItemsets() {
        List<Transaction> transactions = miningData.getTransactions();

        int N = transactions.size();
        double minEsup = minExpectedSupportRate * N;

        // Kết quả kiểu Itemset
        List<Itemset> allFrequent = new ArrayList<>();

        // Step 1: Frequent 1-itemsets
        Map<Set<String>, Double> Lk = findFrequent1Itemsets(transactions, minEsup);

        // Convert sang Itemset
        for (var entry : Lk.entrySet()) {
            allFrequent.add(convertToItemset(entry.getKey(), entry.getValue()));
        }

        // Apriori loop
        while (!Lk.isEmpty()) {

            // Generate candidates
            Set<Set<String>> Ck = aprioriGen(Lk.keySet());

            // Compute expected support
            Map<Set<String>, Double> CkEsup = computeEsup(transactions, Ck);

            // Filter to get next Lk
            Map<Set<String>, Double> nextLk = new HashMap<>();
            for (var e : CkEsup.entrySet()) {
                if (e.getValue() >= minEsup) {
                    nextLk.put(e.getKey(), e.getValue());

                    // Đồng thời add vào output
                    allFrequent.add(convertToItemset(e.getKey(), e.getValue()));
                }
            }

            Lk = nextLk;
        }

        return allFrequent;
    }

    // -----------------------------------------------------
    // STEP 1: Frequent 1-itemsets
    // -----------------------------------------------------
    private Map<Set<String>, Double> findFrequent1Itemsets(List<Transaction> transactions, double minEsup) {
        Map<String, Double> esupMap = new HashMap<>();

        for (Transaction T : transactions) {
            for (Unit u : T.getUnits()) {
                String item = u.getItem().getId();
                double p = u.getProbability();

                esupMap.put(item, esupMap.getOrDefault(item, 0.0) + p);
            }
        }

        Map<Set<String>, Double> result = new HashMap<>();
        for (var entry : esupMap.entrySet()) {
            if (entry.getValue() >= minEsup) {
                result.put(Set.of(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    // -----------------------------------------------------
    // STEP 2: Apriori candidate generation with subset pruning
    // -----------------------------------------------------
    private Set<Set<String>> aprioriGen(Set<Set<String>> Lk) {
        Set<Set<String>> candidates = new HashSet<>();

        List<Set<String>> list = new ArrayList<>(Lk);
        int k = list.get(0).size(); // Lk is k-itemsets → generating (k+1)-itemsets

        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {

                List<String> a = new ArrayList<>(list.get(i));
                List<String> b = new ArrayList<>(list.get(j));

                // sort to compare prefixes
                Collections.sort(a);
                Collections.sort(b);

                // Check: first k-1 items are identical
                if (prefixEqual(a, b, k - 1)) {
                    // Join to create k+1-candidate
                    Set<String> union = new HashSet<>(list.get(i));
                    union.addAll(list.get(j));

                    // Subset pruning: all (k)-subsets must be in Lk
                    if (allSubsetsFrequent(union, Lk)) {
                        candidates.add(union);
                    }
                }
            }
        }

        return candidates;
    }

    private boolean prefixEqual(List<String> a, List<String> b, int prefixLen) {
        for (int i = 0; i < prefixLen; i++) {
            if (!a.get(i).equals(b.get(i))) return false;
        }
        return true;
    }

    private boolean allSubsetsFrequent(Set<String> candidate, Set<Set<String>> Lk) {
        List<String> items = new ArrayList<>(candidate);
        for (int i = 0; i < items.size(); i++) {
            Set<String> subset = new HashSet<>(items);
            subset.remove(items.get(i));
            if (!Lk.contains(subset)) return false;
        }
        return true;
    }

    // -----------------------------------------------------
    // STEP 3: Expected support computation
    // esup(X) = ΣT Π(i∈X) p(i,T)
    // -----------------------------------------------------
    private Map<Set<String>, Double> computeEsup(List<Transaction> transactions, Set<Set<String>> candidates) {
        Map<Set<String>, Double> esupMap = new HashMap<>();

        for (Transaction T : transactions) {
            // Build map item → probability for quick lookup
            Map<String, Double> probMap = new HashMap<>();
            for (Unit u : T.getUnits()) {
                probMap.put(u.getItem().getId(), u.getProbability());
            }

            for (Set<String> X : candidates) {

                boolean containsAll = true;
                double p = 1.0;

                for (String item : X) {
                    Double pi = probMap.get(item);
                    if (pi == null) {
                        containsAll = false;
                        break;
                    }
                    p *= pi;
                }

                if (containsAll) {
                    esupMap.merge(X, p, Double::sum);
                }
            }
        }

        return esupMap;
    }

    private Itemset convertToItemset(Set<String> itemIds, Double esup) {
        Set<Item> items = new HashSet<>();
        for (String id : itemIds) {
            items.add(new Item(id));
            // Nếu class Item của bạn có constructor khác thì sửa lại cho phù hợp
        }
        return new Itemset(items, esup);
    }
}
