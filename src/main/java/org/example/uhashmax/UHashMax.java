package org.example.uhashmax;

import java.util.*;
import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Unit;
import org.example.data.Item;
import org.example.data.Itemset;
/**
 * HashMax-style mining maximal frequent itemsets for an uncertain database
 * using expected support.
 *
 * - expectedSupport(X) = Σ_t Π_{i ∈ X} P(i in t)
 * - một itemset là frequent nếu expectedSupport(X) >= minEsupThreshold
 *   (minEsupThreshold = minEsupRatio * số giao dịch)
 * - maximal: không tồn tại superset frequent nào của X
 */


public class UHashMax {

    private final int maxK;
    private final MiningData data;
    private final double minEsupRatio;      // ví dụ 0.3 = 30%
    private final int numBuckets;           // số bucket cho hashing

    public UHashMax(MiningData data, double minEsupRatio, int numBuckets,int maxK) {
        this.data = data;
        this.minEsupRatio = minEsupRatio;
        this.numBuckets = numBuckets;
        this.maxK = maxK;
    }

    public List<Itemset> run() {
        List<Transaction> transactions = data.getTransactions();
        int n = transactions.size();
        double minEsupThreshold = n * minEsupRatio;

        // 1. Tính expected support cho từng item đơn (F1)
        Map<Item, Double> f1 = buildF1(transactions);

        // Lọc theo ngưỡng
        Map<Item, Double> frequentItems = new HashMap<>();
        for (Map.Entry<Item, Double> e : f1.entrySet()) {
            if (e.getValue() >= minEsupThreshold) {
                frequentItems.put(e.getKey(), e.getValue());
            }
        }
        if (frequentItems.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Prune transaction: chỉ giữ Unit có item frequent
        List<List<Unit>> prunedTransactions = pruneTransactions(transactions, frequentItems.keySet());

        int maxLen = 0;
        for (List<Unit> t : prunedTransactions) {
            if (t.size() > maxLen) maxLen = t.size();
        }
        if (maxLen == 0) {
            return Collections.emptyList();
        }

        List<Itemset> maximalItemsets = new ArrayList<>();

        // 3. Top-down: từ độ dài maxLen xuống 2
        int upperK=Math.min(maxLen,maxK);
        for (int k = upperK; k >= 2; k--) {
            // mỗi bucket: Map<Set<Item>, Double expectedSupport>
            List<Map<Set<Item>, Double>> buckets = new ArrayList<>(numBuckets);
            for (int i = 0; i < numBuckets; i++) {
                buckets.add(new HashMap<>());
            }

            // 3.1. Đếm expected support cho các k-itemset candidates bằng hashing
            for (List<Unit> txUnits : prunedTransactions) {
                if (txUnits.size() < k) continue;

                // sinh k-combination theo Item (sử dụng index trong danh sách Unit)
                List<List<Unit>> unitCombinations = new ArrayList<>();
                generateUnitCombinations(txUnits, k, 0, new ArrayList<>(), unitCombinations);

                for (List<Unit> comb : unitCombinations) {
                    // tính P(X ⊆ t) = ∏ probability
                    double prob = 1.0;
                    Set<Item> items = new HashSet<>();
                    for (Unit u : comb) {
                        prob *= u.getProbability();
                        items.add(u.getItem());
                    }
                    if (prob <= 0.0) continue;

                    int h = Math.abs(items.hashCode()) % numBuckets;
                    Map<Set<Item>, Double> bucket = buckets.get(h);
                    bucket.put(items, bucket.getOrDefault(items, 0.0) + prob);
                }
            }

            // 3.2. Lọc frequent & kiểm tra maximal
            for (Map<Set<Item>, Double> bucket : buckets) {
                for (Map.Entry<Set<Item>, Double> e : bucket.entrySet()) {
                    Set<Item> items = e.getKey();
                    double esup = e.getValue();

                    if (esup < minEsupThreshold) continue;

                    Itemset candidate = new Itemset(items);
                    candidate.setExpectedSupport(esup);

                    // nếu candidate là subset của một maximal đã có → bỏ
                    if (isSubsetOfExistingMaximal(candidate, maximalItemsets)) {
                        continue;
                    }
                    // ngược lại, đây là một maximal mới
                    maximalItemsets.add(candidate);
                }
            }
        }

        // 4. (tuỳ chọn) xét thêm 1-itemset frequent nếu không có MFS dài hơn
        if (maximalItemsets.isEmpty()) {
            for (Map.Entry<Item, Double> e : f1.entrySet()) {
                if (e.getValue() >= minEsupThreshold) {
                    Set<Item> s = new HashSet<>();
                    s.add(e.getKey());
                    Itemset is = new Itemset(s);
                    is.setExpectedSupport(e.getValue());
                    maximalItemsets.add(is);
                }
            }
        }

        return maximalItemsets;
    }

    // ====== HÀM PHỤ ======

    // expected support cho từng item đơn
    private Map<Item, Double> buildF1(List<Transaction> transactions) {
        Map<Item, Double> esup = new HashMap<>();
        for (Transaction t : transactions) {
            for (Unit u : t.getUnits()) {
                Item item = u.getItem();
                double p = u.getProbability();
                esup.put(item, esup.getOrDefault(item, 0.0) + p);
            }
        }
        return esup;
    }

    // bỏ các Unit có item không frequent
    private List<List<Unit>> pruneTransactions(List<Transaction> transactions, Set<Item> frequentItems) {
        List<List<Unit>> pruned = new ArrayList<>();
        for (Transaction t : transactions) {
            List<Unit> filtered = new ArrayList<>();
            for (Unit u : t.getUnits()) {
                if (frequentItems.contains(u.getItem())) {
                    filtered.add(u);
                }
            }
            // để ổn định hash, sort theo item id
            filtered.sort(Comparator.comparing(u -> u.getItem().getId()));
            if (!filtered.isEmpty()) {
                pruned.add(filtered);
            }
        }
        return pruned;
    }

    // sinh combination k phần tử từ danh sách Unit
    private void generateUnitCombinations(List<Unit> units,
                                          int k,
                                          int start,
                                          List<Unit> current,
                                          List<List<Unit>> output) {
        if (current.size() == k) {
            output.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i <= units.size() - (k - current.size()); i++) {
            current.add(units.get(i));
            generateUnitCombinations(units, k, i + 1, current, output);
            current.remove(current.size() - 1);
        }
    }

    // candidate có là subset của một maximal nào đã có không?
    private boolean isSubsetOfExistingMaximal(Itemset candidate, List<Itemset> maximalItemsets) {
        Set<Item> candItems = candidate.getItems();
        for (Itemset m : maximalItemsets) {
            if (m.getItems().containsAll(candItems)) {
                return true;
            }
        }
        return false;
    }
}

