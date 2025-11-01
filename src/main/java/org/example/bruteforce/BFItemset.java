package org.example.bruteforce;

import org.example.data.Item;

import java.util.Set;

public class BFItemset {
    private final Set<Item> items;
    private Double expectedSupport;

    public BFItemset(Set<Item> items) {
        this.items = items;
    }

    public Set<Item> getItems() {
        return items;
    }

    public Double getExpectedSupport() {
        return expectedSupport;
    }

    public void setExpectedSupport(Double expectedSupport) {
        this.expectedSupport = expectedSupport;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.getId()).append("-");
        }
        sb.deleteCharAt(sb.length() - 1);
        if(expectedSupport != null) {
            sb.append(" (esup: ").append(expectedSupport).append(")");
        }
        return sb.toString();
    }
}
