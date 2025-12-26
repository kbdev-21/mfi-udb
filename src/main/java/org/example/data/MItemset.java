package org.example.data;

import java.util.Set;

public class MItemset {
    private Set<String> items;
    private Double exSup;

    public MItemset(Set<String> items, Double exSup) {
        this.items = items;
        this.exSup = exSup;
    }

    public Set<String> getItems() {
        return items;
    }

    public Double getExSup() {
        return exSup;
    }

    public void setExSup(Double exSup) {
        this.exSup = exSup;
    }
}
