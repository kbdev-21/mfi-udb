package org.example.ugenmax;

import java.util.LinkedHashMap;

public class MTransaction {
    private String id;
    private LinkedHashMap<String, Double> units = new LinkedHashMap<>();

    public MTransaction(String id, LinkedHashMap<String, Double> units) {
        this.id = id;
        this.units = units;
    }

    public String getId() {
        return id;
    }

    public LinkedHashMap<String, Double> getUnits() {
        return units;
    }

    public void setUnits(LinkedHashMap<String, Double> units) {
        this.units = units;
    }
}
