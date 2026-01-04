package org.example.uhashmax;

import java.util.*;
import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Unit;
import org.example.data.Item;
import org.example.data.Itemset;

/**
 * UHashMax optimized for 10000+ transactions with combination limit
 */
public class UHashMax {

    private final int maxK;
    private final MiningData data;
    private final double minEsupRatio;
    private final int numBuckets;
    private Map<Item, Integer> itemToIndex;
    private static final int MAX_COMBINATIONS_PER_TX = 12000; // Giới hạn combinations

    public UHashMax(MiningData data, double minEsupRatio, int numBuckets, int maxK) {
        this.data = data;
        this.minEsupRatio = minEsupRatio;
        this.numBuckets = numBuckets;
        this.maxK = maxK;
    }

    /*/
    aaaa
     */
    public List<Itemset> run() {
        long startTime = System.currentTimeMillis();
        List<Transaction> transactions = data.getTransactions();
        int n = transactions.size();
        double minEsupThreshold = n * minEsupRatio;

        System.out.println("Starting UHashMax with " + n + " transactions, minSup=" + minEsupRatio);

        // 1. Tính expected support cho từng item đơn (F1)

        Map<Item, Double> f1 = buildF1(transactions);

        Map<Item, Double> frequentItems = new HashMap<>();
        for (Map.Entry<Item, Double> e : f1.entrySet()) {
            if (e.getValue() >= minEsupThreshold) {
                frequentItems.put(e.getKey(), e.getValue());
            }
        }
        if (frequentItems.isEmpty()) {
            return Collections.emptyList();
        }

        System.out.println("Frequent 1-items: " + frequentItems.size());
        buildItemIndex(frequentItems);

        // 2. Prune transactions
        // Xóa items không frequent và sort giảm dần theo độ dài
        List<List<Unit>> prunedTransactions = pruneTransactions(transactions, frequentItems.keySet());
        prunedTransactions.sort((a, b) -> Integer.compare(b.size(), a.size()));

        int maxLen = prunedTransactions.isEmpty() ? 0 : prunedTransactions.get(0).size();
        if (maxLen == 0) {
            return Collections.emptyList();
        }

        System.out.println("Max transaction length: " + maxLen);

        List<Itemset> maximalItemsets = new ArrayList<>();

        // 3. Top-down mining
        //Set upperK để mining từ dài nhất đến 2
        int upperK = Math.min(maxLen, maxK);
        for (int k = upperK; k >= 2; k--) {
            long kStart = System.currentTimeMillis();

            // Filter transactions by length
            List<List<Unit>> validTransactions = new ArrayList<>();
            for (List<Unit> tx : prunedTransactions) {
                if (tx.size() >= k) {
                    validTransactions.add(tx);
                } else {
                    break;
                }
            }

            if (validTransactions.isEmpty()) {
                continue;
            }

            System.out.println("Processing k=" + k + ", valid transactions: " + validTransactions.size());

            // Khởi tạo buckets với HashMap thông thường
            List<Map<Set<Item>, Double>> buckets = new ArrayList<>(numBuckets);
            for (int i = 0; i < numBuckets; i++) {
                buckets.add(new HashMap<>());
            }

            // 3.1. Đếm expected support - với giới hạn combinations
            int processedTx = 0;
            for (List<Unit> txUnits : validTransactions) {
                if (txUnits.size() < k) continue;

                // OPTIMIZATION: Skip transaction có quá nhiều combinations
                long possibleCombs = binomialCoefficient(txUnits.size(), k);
                if (possibleCombs > MAX_COMBINATIONS_PER_TX) {
                    continue; // Skip transaction này để tránh treo
                }

                List<List<Unit>> unitCombinations = new ArrayList<>();
                generateUnitCombinations(txUnits, k, 0, new ArrayList<>(), unitCombinations);

                for (List<Unit> comb : unitCombinations) {
                    double prob = 1.0;
                    Set<Item> items = new HashSet<>();
                    for (Unit u : comb) {
                        prob *= u.getProbability();
                        items.add(u.getItem());
                    }

                    if (prob < 0.00001) continue;

                    int h = Math.abs(items.hashCode()) % numBuckets;
                    Map<Set<Item>, Double> bucket = buckets.get(h);
                    bucket.put(items, bucket.getOrDefault(items, 0.0) + prob);
                }

                processedTx++;
                if (processedTx % 1000 == 0) {
                    System.out.println("  Processed " + processedTx + "/" + validTransactions.size() + " transactions");
                }
            }

            // 3.2. Lọc frequent & kiểm tra maximal
            int candidateCount = 0;
            for (Map<Set<Item>, Double> bucket : buckets) {
                if (bucket.isEmpty()) continue;

                for (Map.Entry<Set<Item>, Double> e : bucket.entrySet()) {
                    Set<Item> items = e.getKey();
                    double esup = e.getValue();

                    if (esup < minEsupThreshold) continue;

                    candidateCount++;

                    if (isSubsetOfExistingMaximalFast(items, maximalItemsets)) {
                        continue;
                    }

                    Itemset candidate = new Itemset(items);
                    candidate.setExpectedSupport(esup);
                    maximalItemsets.add(candidate);
                }
            }

            // Periodic cleanup
            if (k % 3 == 0 && maximalItemsets.size() > 100) {
                removeNonMaximal(maximalItemsets);
            }

            buckets.clear();
            long kEnd = System.currentTimeMillis();
            System.out.println("k=" + k + " done in " + (kEnd - kStart) + "ms, candidates: " + candidateCount + ", maximal: " + maximalItemsets.size());
        }

        // Final cleanup
        removeNonMaximal(maximalItemsets);

        if (maximalItemsets.isEmpty()) {
            for (Map.Entry<Item, Double> e : frequentItems.entrySet()) {
                Set<Item> s = new HashSet<>();
                s.add(e.getKey());
                Itemset is = new Itemset(s);
                is.setExpectedSupport(e.getValue());
                maximalItemsets.add(is);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime - startTime) + "ms");
        System.out.println("Final maximal itemsets: " + maximalItemsets.size());

        return maximalItemsets;
    }

    // ====== HELPER METHODS ======

    /**
     *
     *Tính expected support của từng item đơn lẻ
     */
    private Map<Item, Double> buildF1(List<Transaction> transactions) {
        Map<Item, Double> esup = new HashMap<>();
        //Tạo hashmap để lưu expected support của từng item
        for (Transaction t : transactions) {
            for (Unit u : t.getUnits()) {
                esup.merge(u.getItem(), u.getProbability(), Double::sum);
                //Lấy các item trong các unit và tính tổng các Prob
            }
        }
        return esup;
    }

    /**
     *
     * Hàm tính số cách chọn k phần tử từ n phần tử C(n,k)
     */
    private long binomialCoefficient(int n, int k) {
        if (k > n - k) k = n - k;
        long result = 1;
        for (int i = 0; i < k; i++) {
            result *= (n - i);
            result /= (i + 1);
            if (result > MAX_COMBINATIONS_PER_TX) return result;
        }
        return result;
    }

    /**
     *
     * Hàm này tạo index cố định để thực hiện hàm toBitSet
     */
    private void buildItemIndex(Map<Item, Double> frequentItems) {
        itemToIndex = new HashMap<>();
        int idx = 0;
        for (Item item : frequentItems.keySet()) {
            itemToIndex.put(item, idx++);
        }
    }

    /**
     *
     * Hàm  chuyển một tập các Item (tập mục) thành một BitSet
     */
    private BitSet toBitSet(Set<Item> items) {
        BitSet bits = new BitSet();
        //Tạo 1 BitSet mới, ban đầu các bit đều false
        for (Item item : items) {
            Integer idx = itemToIndex.get(item);
            // Lấy chỉ số index tương ứng với item từ Map<Item, Integer> ItemtoIndex
            if (idx != null) {
                bits.set(idx);

            }
        }
        return bits;
        //Trả về BitSet đã được thiết lập các bit tương ứng với các Item trong tập items.
    }

    /**
     * Loại bỏ item không phổ biến khỏi từng transaction
     * Loại bỏ luôn Transaction rỗng
     * Trả về danh sách các Transaction đã làm sạch,chỉ chứa các item frequent
     */

    private List<List<Unit>> pruneTransactions(List<Transaction> transactions, Set<Item> frequentItems) {
        List<List<Unit>> pruned = new ArrayList<>();
        //Tạo list để lưu các giao dịch được cắt tỉa
        for (Transaction t : transactions) {
            List<Unit> filtered = new ArrayList<>();
            for (Unit u : t.getUnits()) {
                if (frequentItems.contains(u.getItem())) {
                    filtered.add(u);
                    //Nếu u.getItem() là item trong frequentItems thì lưu vào filtered, không thì loại bỏ
                }
            }
            filtered.sort(Comparator.comparing(u -> u.getItem().getId()));
            // sắp xếp theo
            if (!filtered.isEmpty()) {
                pruned.add(filtered);
                //Nếu sau khi cắt tỉa, filtered vẫn còn ít nhất một Unit → thêm filtered vào pruned.
                // filtered rỗng (hết giao dịch frequent) -> bỏ qua giao dịch đó
            }
        }
        return pruned;
        // trả về danh sách các giao dịch frequent
    }

    /**
     *
     * Tạo ra tất cả các tổ hợp con gồm k phần tử từ unit
     * Dùng đệ quy
     */
    private void generateUnitCombinations(List<Unit> units, int k, int start,
                                          List<Unit> current, List<List<Unit>> output) {
        if (current.size() == k) {
            output.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i <= units.size() - (k - current.size()); i++) {
            //Điều kiện : units.size() - (k - current.size()) là vị trí cuối cùng mà nếu chọn units.get(i)thì vẫn đủ phần tử phía sau để chọn đủ k phần tử
            //Nếu i lớn hơn thì phía sau không còn đủ phần tử để chọn -> không thể tạo tổ hợp k phần tử -> không cần xét
            current.add(units.get(i));
            //Gọi đệ quy tổ hợp tiếp theo
            generateUnitCombinations(units, k, i + 1, current, output);

            current.remove(current.size() - 1);
            //remove có 2 tác dụng
            //Trong đệ quy thì remove phần tử thêm vào
            //Ngoài đệ quy thì remove phần tử gốc
        }
    }

    /**
     *
     * Hàm isSubsetOfExistingMaximalFast check xem canItems có phải là tập con (subset) của 1 maximal itemset nào đã có sẵn trong maximalItemsets không,nếu có thì loại canItems luôn .
     */
    private boolean isSubsetOfExistingMaximalFast(Set<Item> candItems, List<Itemset> maximalItemsets) {
        BitSet candBits = toBitSet(candItems);
        for (Itemset m : maximalItemsets) {
            if (m.getItems().size() < candItems.size()) continue;

            BitSet maxBits = toBitSet(m.getItems());
            BitSet temp = (BitSet) candBits.clone();
            temp.and(maxBits);
            //BitSet chứa các bit có trong cả canItems và m.getItems()

            if (temp.equals(candBits)) {
                return true;
            }
        }
        return false;
    }


    /**
     *
     * Hàm removeNonMaximal để lọc các itemset không là maximal từ list maximalItemsets,để đảm bảo list output chỉ còn các maximalItemsets thật sự
     */
    private void removeNonMaximal(List<Itemset> maximalItemsets) {
        Set<Itemset> toRemove = new HashSet<>();
        for (int i = 0; i < maximalItemsets.size(); i++) {
            Itemset current = maximalItemsets.get(i);
            for (int j = 0; j < maximalItemsets.size(); j++) {
                if (i != j) {
                    Itemset other = maximalItemsets.get(j);
                    if (other.getItems().size() > current.getItems().size() &&
                            other.getItems().containsAll(current.getItems())) {
                        toRemove.add(current);
                        break;
                    }
                }
            }
        }
        maximalItemsets.removeAll(toRemove);
    }
}
