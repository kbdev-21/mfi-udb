package org.example;

import org.example.data.Item;
import org.example.data.Transaction;
import org.example.data.Unit;
import org.example.kb.MTransaction;

import java.util.ArrayList;
import java.util.List;

public class Mapper {
    public List<Transaction> mapFrom(List<MTransaction> mTransactions) {
        return mTransactions.stream().map(t -> {
            List<Unit> units = new ArrayList<>();
            for(var x: t.getUnits().entrySet()) {
                Unit unit = new Unit(
                    new Item(x.getKey()),
                    x.getValue()
                );
                units.add(unit);
            }
            return new Transaction(units);
        }).toList();
    }
}
