package org.example.ufpgrowth.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class UFPTreeNode {
    private final String itemId;
    private final double probability;
    private int appearance;
    private final UFPTreeNode parent;
    private final Map<String, UFPTreeNode> children;
    private UFPTreeNode neighbor;

    public UFPTreeNode(String itemId, double probability, UFPTreeNode parent) {
        this.itemId = itemId;
        this.probability = probability;
        this.appearance = 1;
        this.parent = parent;
        this.children = new LinkedHashMap<>();
    }

    public String getItemId() { return itemId; }
    public double getProbability() { return probability; }
    public int getAppearance() { return appearance; }
    public UFPTreeNode getParent() { return parent; }
    public Map<String, UFPTreeNode> getChildren() { return children; }
    public UFPTreeNode getNeighbor() { return neighbor; }

    public void incrementAppearance() { this.appearance++; }

    public void setNeighbor(UFPTreeNode neighbor) { this.neighbor = neighbor; }

    public UFPTreeNode addChild(String itemId, double probability) {
        UFPTreeNode child = new UFPTreeNode(itemId, probability, this);
        String key = itemId + ":" + probability;
        this.children.put(key, child);
        return child;
    }
}
