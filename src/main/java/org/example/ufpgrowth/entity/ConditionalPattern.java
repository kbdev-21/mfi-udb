package org.example.ufpgrowth.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditionalPattern {
    private String itemId;
    private double baseProbability;
    private Map<String, Double> pattern = new LinkedHashMap<>();

    public ConditionalPattern(){}

    public ConditionalPattern(String itemId, double baseProbability, Map<String, Double> pattern) {
        this.itemId = itemId;
        this.baseProbability = baseProbability;
        this.pattern = pattern;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getBaseProbability() {
        return baseProbability;
    }

    public void setBaseProbability(double baseProbability) {
        this.baseProbability = baseProbability;
    }

    public Map<String, Double> getPattern() {
        return pattern;
    }

    public void setPattern(Map<String, Double> pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(itemId).append(":").append(baseProbability).append(" => ");
        pattern.forEach((k, v) -> builder.append(k).append(":").append(v).append(", "));
        builder.replace(builder.length() - 2, builder.length(), "");
        return builder.toString();
    }
}
