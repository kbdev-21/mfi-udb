package org.example.uhashmax;

import org.example.data.MItemset;
import org.example.data.MTransaction;

import java.util.*;

/**
 * HashMax-style mining of MAXIMAL frequent itemsets on UNCERTAIN DB using EXPECTED SUPPORT.
 *
 * Expected Support:
 *   ES(X) = sum_{T in DB} prod_{i in X} p(i, T)
 *
 * Frequent(X) iff ES(X) >= minEsup (absolute threshold, same as your GenMax call):
 *   minEsup = dataset.size() * 0.3
 *
 * Key points:
 * - Bucket identical transactions to reduce duplicates.
 * - Prune F1 (singletons) first.
 * - Build frequent pairs AFTER pruning F1 for Clean().
 * - sources(X) computed exactly by intersection of bucketsContainingItem[item].
 * - Post-filter maximal to guarantee "maximal" correctness.
 */
public final class UHashMax {

    private UHashMax() {}

    /** Main API */
    public static List<MItemset> mfi(List<MTransaction> dataset, double minEsup) {
        return mfi(dataset, minEsup, 1e-9);
    }

    public static List<MItemset> mfi(List<MTransaction> dataset, double minEsup, double eps) {
        if (dataset == null || dataset.isEmpty()) return List.of();

        // 1) Collect all item IDs (stable order)
        TreeSet<String> itemSet = new TreeSet<>();
        for (MTransaction t : dataset) {
            if (t == null || t.getUnits() == null) continue;
            itemSet.addAll(t.getUnits().keySet());
        }
        if (itemSet.isEmpty()) return List.of();

        List<String> idxToItem = new ArrayList<>(itemSet);
        int nItems = idxToItem.size();
        Map<String, Integer> itemToIdx = new HashMap<>(nItems * 2);
        for (int i = 0; i < nItems; i++) itemToIdx.put(idxToItem.get(i), i);

        // 2) Build buckets + F1 expected support in one pass over dataset
        double[] f1ES = new double[nItems];

        Map<TxKey, Bucket> bucketMap = new HashMap<>();
        List<Bucket> buckets = new ArrayList<>();

        for (MTransaction t : dataset) {
            if (t == null || t.getUnits() == null || t.getUnits().isEmpty()) continue;

            // Convert transaction units into sorted index arrays
            int m = t.getUnits().size();
            int[] idxs = new int[m];
            double[] probs = new double[m];

            int k = 0;
            for (Map.Entry<String, Double> e : t.getUnits().entrySet()) {
                Integer idx = itemToIdx.get(e.getKey());
                if (idx == null) continue;
                double p = clamp01(e.getValue());
                idxs[k] = idx;
                probs[k] = p;
                k++;
            }
            if (k == 0) continue;

            if (k < m) {
                idxs = Arrays.copyOf(idxs, k);
                probs = Arrays.copyOf(probs, k);
            }

            // Sort by idx (and permute probs accordingly)
            sortByIdx(idxs, probs);

            // Update F1 ES
            for (int i = 0; i < idxs.length; i++) {
                f1ES[idxs[i]] += probs[i];
            }

            // Bucket by exact signature (items + prob bits)
            TxKey key = new TxKey(idxs, probs);
            Bucket b = bucketMap.get(key);
            if (b == null) {
                double[] probByItem = new double[nItems];
                for (int i = 0; i < idxs.length; i++) probByItem[idxs[i]] = probs[i];
                b = new Bucket(buckets.size(), idxs, probByItem);
                bucketMap.put(key, b);
                buckets.add(b);
            } else {
                b.count++;
            }
        }

        if (buckets.isEmpty()) return List.of();

        // 3) Prune F1
        boolean[] inF1 = new boolean[nItems];
        for (int i = 0; i < nItems; i++) {
            inF1[i] = (f1ES[i] + eps) >= minEsup;
        }

        // 4) Build bucketsContainingItem and pruned items per bucket
        BitSet[] bucketsContainingItem = new BitSet[nItems];
        for (int i = 0; i < nItems; i++) bucketsContainingItem[i] = new BitSet(buckets.size());

        int[][] prunedItemsPerBucket = new int[buckets.size()][];
        for (Bucket b : buckets) {
            int[] pruned = filterByF1(b.items, inF1);
            prunedItemsPerBucket[b.id] = pruned;
            for (int it : pruned) bucketsContainingItem[it].set(b.id);
        }

        // 5) Compute pair ES only after pruning F1 (much faster), then frequentPairs for Clean()
        Map<Long, Double> pairES = new HashMap<>();
        for (Bucket b : buckets) {
            int[] pruned = prunedItemsPerBucket[b.id];
            if (pruned.length < 2) continue;

            for (int i = 0; i < pruned.length; i++) {
                int a = pruned[i];
                double pa = b.probByItem[a];
                if (pa <= 0) continue;

                for (int j = i + 1; j < pruned.length; j++) {
                    int c = pruned[j];
                    double pc = b.probByItem[c];
                    if (pc <= 0) continue;

                    long pk = pairKey(a, c);
                    pairES.merge(pk, b.count * pa * pc, Double::sum);
                }
            }
        }

        Set<Long> frequentPairs = new HashSet<>();
        for (Map.Entry<Long, Double> e : pairES.entrySet()) {
            if (e.getValue() + eps >= minEsup) frequentPairs.add(e.getKey());
        }

        // 6) Initialize candidates Ck from pruned bucket itemsets (sources computed EXACT by intersection)
        Map<Integer, Map<ItemsetKey, Candidate>> C = new HashMap<>();
        int maxSize = 0;

        for (Bucket b : buckets) {
            int[] pruned = prunedItemsPerBucket[b.id];
            if (pruned.length == 0) continue;

            if (upperBoundBySingletons(pruned, f1ES) + eps < minEsup) continue;

            BitSet src = computeSources(pruned, bucketsContainingItem);
            if (src.isEmpty()) continue;

            maxSize = Math.max(maxSize, pruned.length);
            addOrMergeCandidate(C, new Candidate(pruned, 0, src));
        }

        // 7) Top-down HashMax loop (k from maxSize down to 3)
        List<BitSet> maximalBits = new ArrayList<>();
        List<Found> found = new ArrayList<>();

        for (int k = maxSize; k >= 3; k--) {
            Map<ItemsetKey, Candidate> Ck = C.get(k);
            if (Ck == null || Ck.isEmpty()) continue;

            List<Candidate> snapshot = new ArrayList<>(Ck.values());

            for (Candidate t : snapshot) {
                double es = expectedSupport(t, buckets);
                if (es + eps >= minEsup) {
                    BitSet bits = bitsetOf(t.items);
                    maximalBits.add(bits);
                    found.add(new Found(t.items, es));
                    continue;
                }

                // Not frequent -> generate subsets size k-1
                int n = t.items.length;
                for (int removeIdx = 0; removeIdx < n; removeIdx++) {
                    int[] subItems = removeAt(t.items, removeIdx);
                    if (subItems.length <= 2) continue;

                    Candidate sub = new Candidate(subItems, removeIdx, new BitSet());

                    // Clean using frequent pairs
                    clean(sub, frequentPairs);
                    if (sub.items.length <= 2) continue;

                    if (upperBoundBySingletons(sub.items, f1ES) + eps < minEsup) continue;

                    // Recompute sources EXACT after Clean
                    sub.sources = computeSources(sub.items, bucketsContainingItem);
                    if (sub.sources.isEmpty()) continue;

                    // Maximality prune
                    BitSet subBits = bitsetOf(sub.items);
                    if (isSubsetOfAny(subBits, maximalBits)) continue;

                    addOrMergeCandidate(C, sub);
                }
            }

            C.remove(k); // free memory
        }

        // 8) Add maximal size-2
        List<Found> pairs = new ArrayList<>();
        for (long pk : frequentPairs) {
            int a = hi(pk), b = lo(pk);
            BitSet bits = new BitSet();
            bits.set(a); bits.set(b);
            if (!isSubsetOfAny(bits, maximalBits)) {
                double es = pairES.getOrDefault(pk, 0.0);
                pairs.add(new Found(new int[]{Math.min(a, b), Math.max(a, b)}, es));
            }
        }

        // 9) Add maximal singletons
        List<BitSet> higher = new ArrayList<>(maximalBits);
        for (Found p : pairs) higher.add(bitsetOf(p.items));

        List<Found> singles = new ArrayList<>();
        for (int i = 0; i < nItems; i++) {
            if (!inF1[i]) continue;
            BitSet bits = new BitSet();
            bits.set(i);
            if (!isSubsetOfAny(bits, higher)) singles.add(new Found(new int[]{i}, f1ES[i]));
        }

        List<Found> all = new ArrayList<>();
        all.addAll(found);
        all.addAll(pairs);
        all.addAll(singles);

        if (all.isEmpty()) return List.of();

        // 10) Dedup
        Map<ItemsetKey, Found> dedup = new HashMap<>();
        for (Found f : all) {
            ItemsetKey key = new ItemsetKey(f.items);
            Found prev = dedup.get(key);
            if (prev == null || f.es > prev.es) dedup.put(key, f);
        }

        // 11) Post-filter maximal to guarantee maximal correctness
        List<Found> dedupList = new ArrayList<>(dedup.values());
        dedupList.sort((x, y) -> Integer.compare(y.items.length, x.items.length));

        List<BitSet> keptBits = new ArrayList<>();
        List<Found> kept = new ArrayList<>();
        for (Found f : dedupList) {
            BitSet bs = bitsetOf(f.items);
            if (isSubsetOfAny(bs, keptBits)) continue;
            keptBits.add(bs);
            kept.add(f);
        }

        // 12) Convert to MItemset (Set<String>, exSup)
        List<MItemset> out = new ArrayList<>(kept.size());
        for (Found f : kept) {
            LinkedHashSet<String> ids = new LinkedHashSet<>();
            for (int idx : f.items) ids.add(idxToItem.get(idx));
            out.add(new MItemset(ids, f.es));
        }

        // Stable sorting: longer first
        out.sort((a, b) -> Integer.compare(b.getItems().size(), a.getItems().size()));
        return out;
    }

    /*  Core computations  */
    private static double expectedSupport(Candidate c, List<Bucket> buckets) {
        double sum = 0.0;
        for (int bid = c.sources.nextSetBit(0); bid >= 0; bid = c.sources.nextSetBit(bid + 1)) {
            Bucket b = buckets.get(bid);
            double prod = 1.0;

            for (int item : c.items) {
                double p = b.probByItem[item];
                if (p <= 0.0) { prod = 0.0; break; }
                prod *= p;
            }
            if (prod != 0.0) sum += b.count * prod;
        }
        return sum;
    }

    /** sources(X) = tập các bucket (transaction đã bucket hóa) mà chứa đầy đủ itemset X. */
    private static BitSet computeSources(int[] items, BitSet[] bucketsContainingItem) {
        if (items.length == 0) return new BitSet();

        int pivot = items[0];
        int minCard = bucketsContainingItem[pivot].cardinality();
        for (int i = 1; i < items.length; i++) {
            int it = items[i];
                int card = bucketsContainingItem[it].cardinality();
            if (card < minCard) {
                minCard = card;
                pivot = it;
                if (minCard == 0) break;
            }
        }
        if (minCard == 0) return new BitSet();

        BitSet s = (BitSet) bucketsContainingItem[pivot].clone();
        for (int it : items) {
            if (it == pivot) continue;
            s.and(bucketsContainingItem[it]);
            if (s.isEmpty()) break;
        }
        return s;
    }

    /**
     * Clean: keep mandatory prefix; remove optional item x if (m,x) is NOT frequent for any mandatory m.
     */
    private static void clean(Candidate t, Set<Long> frequentPairs) {
        int n = t.items.length;
        int mand = Math.min(t.mandatorySize, n);
        if (mand <= 0 || n <= 2) return;

        boolean[] keep = new boolean[n];
        Arrays.fill(keep, true);

        for (int idx = mand; idx < n; idx++) {
            int x = t.items[idx];
            for (int mi = 0; mi < mand; mi++) {
                int m = t.items[mi];
                long pk = pairKey(m, x);
                if (!frequentPairs.contains(pk)) {
                    keep[idx] = false;
                    break;
                }
            }
        }

        t.items = compactByMask(t.items, keep);
        if (t.mandatorySize > t.items.length) t.mandatorySize = t.items.length;
    }

    /**
     Prune theo maximality(Check theo BitSet Candidate AND NOT M rỗng -> subset
     */
    private static boolean isSubsetOfAny(BitSet candidate, List<BitSet> maximals) {
        for (BitSet m : maximals) {
            BitSet tmp = (BitSet) candidate.clone();
            tmp.andNot(m);
            if (tmp.isEmpty()) return true;
        }
        return false;
    }

    /**
     Loại item không frequent khỏi 1 transaction
     */

    private static int[] filterByF1(int[] items, boolean[] inF1) {
        int[] tmp = new int[items.length];
        int k = 0;
        for (int x : items) if (inF1[x]) tmp[k++] = x;
        return Arrays.copyOf(tmp, k);
    }

    /**
     Sinh subset của X với kích thước X-1 khi X không frequent
     */
    private static int[] removeAt(int[] items, int removeIdx) {
        int[] out = new int[items.length - 1];
        int k = 0;
        for (int i = 0; i < items.length; i++) if (i != removeIdx) out[k++] = items[i];
        return out;
    }

    /**
     Sau khi clean, Tạo ra mảng itemset chỉ giữ những item còn giữ
     */
    private static int[] compactByMask(int[] items, boolean[] keep) {
        int[] tmp = new int[items.length];
        int k = 0;
        for (int i = 0; i < items.length; i++) if (keep[i]) tmp[k++] = items[i];
        return Arrays.copyOf(tmp, k);
    }

    /**
     Chuyển itemset dạng int[] sang BitSet để check nhanh hơn
     */
    private static BitSet bitsetOf(int[] items) {
        BitSet bs = new BitSet();
        for (int x : items) bs.set(x);
        return bs;
    }

    /**
     Hợp nhất các itemset giống các item cho ra cùng 1 key
     */
    private static long pairKey(int a, int b) {
        int x = Math.min(a, b), y = Math.max(a, b);
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    /**
     Tách ngược lại 2 số int từ key long
     */
    private static int hi(long k) { return (int) (k >>> 32); }
    private static int lo(long k) { return (int) (k & 0xffffffffL); }

    /**
     *
     * @param C
     * @param c
     */
    private static void addOrMergeCandidate(Map<Integer, Map<ItemsetKey, Candidate>> C, Candidate c) {
        int size = c.items.length;
        if (size <= 0) return;

        Map<ItemsetKey, Candidate> bucket = C.computeIfAbsent(size, k -> new HashMap<>());
        ItemsetKey key = new ItemsetKey(c.items);

        Candidate prev = bucket.get(key);
        if (prev == null) bucket.put(key, c);
        else {
            prev.sources.or(c.sources);
            prev.mandatorySize = Math.max(prev.mandatorySize, c.mandatorySize);
        }
    }

    /**
     Ép xác suất về kiểu [0,1] tránh null/NaN
     */
    private static double clamp01(Double p) {
        if (p == null || Double.isNaN(p)) return 0.0;
        if (p < 0.0) return 0.0;
        if (p > 1.0) return 1.0;
        return p;
    }

    /** In-place sort idxs ascending and permute probs accordingly (simple quicksort). */
    private static void sortByIdx(int[] idxs, double[] probs) {
        quickSort(idxs, probs, 0, idxs.length - 1);
    }

    /**
     Hàm thủ công để sort song song 2 mảng idxs và probs
     */
    private static void quickSort(int[] a, double[] b, int lo, int hi) {
        int i = lo, j = hi;
        int pivot = a[lo + (hi - lo) / 2];

        while (i <= j) {
            while (a[i] < pivot) i++;
            while (a[j] > pivot) j--;

            if (i <= j) {
                swap(a, b, i, j);
                i++; j--;
            }
        }
        if (lo < j) quickSort(a, b, lo, j);
        if (i < hi) quickSort(a, b, i, hi);
    }

    /**
     Hàm thủ công để sort song song 2 mảng idxs và probs
     */
    private static void swap(int[] a, double[] b, int i, int j) {
        int ti = a[i]; a[i] = a[j]; a[j] = ti;
        double td = b[i]; b[i] = b[j]; b[j] = td;
    }

    private static double upperBoundBySingletons(int[] items, double[] f1ES) {
        double ub = Double.POSITIVE_INFINITY;
        for (int it : items) {
            double v = f1ES[it];
            if (v < ub) ub = v;
        }
        return ub;
    }
}
