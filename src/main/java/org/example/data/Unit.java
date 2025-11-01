package org.example.data;

public class Unit {
    private final Item item;
    private final double probability;

    public Unit(Item item, double probability) {
        this.item = item;
        this.probability = probability;
    }

    public Item getItem() {
        return this.item;
    }

    public double getProbability() {
        return this.probability;
    }
}
