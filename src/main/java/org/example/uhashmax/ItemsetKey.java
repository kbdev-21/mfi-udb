package org.example.uhashmax;


import java.util.Arrays;

/**
 * Khóa băm chuẩn hóa itemset int[] để đưa vào hashmap
 */
final class ItemsetKey {
    final int[] items;
    final int hash;

    ItemsetKey(int[] items) {
        this.items = items;
        this.hash = Arrays.hashCode(items);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ItemsetKey other) && Arrays.equals(items, other.items);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}   