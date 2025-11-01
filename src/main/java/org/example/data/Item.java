package org.example.data;

public class Item {
    private final String id;

    public Item(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}