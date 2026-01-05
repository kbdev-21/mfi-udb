package org.example.uhashmax;

import java.util.Arrays;

/** Key for bucketing identical uncertain transactions: (sorted idxs + prob bits aligned). */
final class TxKey {
    final int[] idxs;
    final long[] probBits;
    final int hash;

    TxKey(int[] idxs, double[] probs) {
        this.idxs = Arrays.copyOf(idxs, idxs.length);
        this.probBits = new long[probs.length];

        int h = Arrays.hashCode(this.idxs);
        for (int i = 0; i < probs.length; i++) {
            long bits = Double.doubleToLongBits(probs[i]);
            probBits[i] = bits;
            h = 31 * h + Long.hashCode(bits);
        }
        this.hash = h;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof TxKey other)) return false;
        return Arrays.equals(idxs, other.idxs) && Arrays.equals(probBits, other.probBits);
    }
    @Override public int hashCode() { return hash; }
}
