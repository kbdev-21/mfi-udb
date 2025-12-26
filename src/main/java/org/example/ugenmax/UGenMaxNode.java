package org.example.ugenmax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UGenMaxNode {
    private Set<String> itemset;
    private Map<String, Double> tidProb;

    public UGenMaxNode() {
        this.itemset = new HashSet<String>();
        this.tidProb = new HashMap<String, Double>();
    }

    public UGenMaxNode(Set<String> itemset, Map<String, Double> tidProb) {
        this.itemset = itemset;
        this.tidProb = tidProb;
    }

    public int getLevel() {
        return itemset.size();
    }

    public Double getEsup() {
        return tidProb.values().stream().reduce(0.0, Double::sum);
    }

    public void addTidProb(String tid, Double prob) {
        tidProb.put(tid, prob);
    }

    /* GETTER SETTER */
    public Set<String> getItemset() {
        return itemset;
    }

    public void setItemset(Set<String> itemset) {
        this.itemset = itemset;
    }

    public Map<String, Double> getTidProb() {
        return tidProb;
    }

    public void setTidProb(Map<String, Double> tidProb) {
        this.tidProb = tidProb;
    }

    @Override
    public String toString() {
        return "UGenMaxNode{" +
            "itemsets=" + itemset +
            ", tidProbabilites=" + tidProb +
            ", esup=" + getEsup() +
            '}';
    }
}
