package org.example.uhashmax;

/* ====================== Internal structures ====================== */
final class Bucket {
    final int id;
    int count = 1;
    final int[] items;          // sorted item indices present
    final double[] probByItem;  // dense prob lookup by item index

    Bucket(int id, int[] items, double[] probByItem) {
        this.id = id;
        this.items = items;
        this.probByItem = probByItem;
    }
}
