package org.example.uhashmax;

import java.util.BitSet;


final class Candidate {
    int[] items;          // sorted
    int mandatorySize;    // prefix length
    BitSet sources;       // bucket IDs containing this itemset

    Candidate(int[] items, int mandatorySize, BitSet sources) {
        this.items = items;
        this.mandatorySize = Math.max(0, mandatorySize);
        this.sources = sources;
    }
}
