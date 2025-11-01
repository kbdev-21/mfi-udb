package org.example.data;

import java.util.List;

public class Transaction {
    private final List<Unit> units;

    public Transaction(List<Unit> units) {
        this.units = units;
    }

    public List<Unit> getUnits() {
        return this.units;
    }
}
