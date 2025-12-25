package org.example.ufpgrowth.entity;

public class ItemNodeEntry {
    private UFPTreeNode head;
    private UFPTreeNode tail;

    public ItemNodeEntry(UFPTreeNode head, UFPTreeNode tail) {
        this.head = head;
        this.tail = tail;
    }

    public void setHead(UFPTreeNode head) {
        this.head = head;
    }

    public void setTail(UFPTreeNode tail) {
        this.tail = tail;
    }

    public UFPTreeNode getHead() { return head; }
    public UFPTreeNode getTail() { return tail; }
}
