package org.example.data;

import java.util.List;

public class Transaction {
    private List<Unit> units;

    public Transaction(List<Unit> units) {
        this.units = units;
    }

    public List<Unit> getUnits() {
        return this.units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}
