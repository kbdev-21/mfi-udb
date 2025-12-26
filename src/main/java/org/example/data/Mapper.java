package org.example.data;

import org.example.ugenmax.MTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapper {
    public List<Transaction> mapFrom(List<MTransaction> mTransactions) {
        Map<String, Item> itemsMap = new HashMap<>();
        return mTransactions.stream().map(t -> {
            List<Unit> units = new ArrayList<>();
            for(var x: t.getUnits().entrySet()) {
                Item item = null;
                if(!itemsMap.containsKey(x.getKey())) {
                    itemsMap.put(x.getKey(), new Item(x.getKey()));
                }
                Unit unit = new Unit(
                    itemsMap.get(x.getKey()),
                    x.getValue()
                );
                units.add(unit);
            }
            return new Transaction(units);
        }).toList();
    }
}
