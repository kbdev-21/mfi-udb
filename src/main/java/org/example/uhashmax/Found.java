package org.example.uhashmax;

/**
 * Gom các kết quả frequent vào gồm k>=3,k=2,k=1
 */
final class Found {
    final int[] items;
    final double es;
    Found(int[] items, double es) { this.items = items; this.es = es; }
}
