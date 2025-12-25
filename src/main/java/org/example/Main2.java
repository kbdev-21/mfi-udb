package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.DataGenerator;
import org.example.data.Itemset;
import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Item;
import org.example.data.Unit;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.HashMaxUncertain.HashMaxUncertain;
import org.example.kb.GenDataset;
import org.example.kb.MTransaction;

public class Main2 {
    private static final Mapper mapper = new Mapper();
    public static void main(String[] args) {
//        // ví dụ tạo dữ liệu nhỏ
//        Item A = new Item("A");
//        Item B = new Item("B");
//        Item C = new Item("C");
//        Item D = new Item("D");
//
//        Transaction t1 = new Transaction(List.of(
//                new Unit(A, 0.9),
//                new Unit(B, 0.8),
//                new Unit(C, 0.7)
//        ));
//
//
//        Transaction t2 = new Transaction(List.of(
//                new Unit(A, 0.6),
//                new Unit(B, 0.6),
//                new Unit(C, 0.5)
//        ));
//
//
//        Transaction t3 = new Transaction(List.of(
//                new Unit(B, 0.9),
//                new Unit(C, 0.8),
//                new Unit(D, 0.7)
//        ));
//
//        MiningData data = new MiningData(
//                List.of(A, B, C, D),
//                List.of(t1, t2, t3)
//        );

        List<MTransaction> genDataset = GenDataset.readFromFile("data/dataset-small.txt");
        List<Transaction> transactions = mapper.mapFrom(genDataset);

        Set<String> itemIds = new HashSet<>();
        for (Transaction t : transactions) {
            for(Unit u : t.getUnits()) {
                itemIds.add(u.getItem().getId());
            }
        }
        List<Item> items = itemIds.stream().map(Item::new).toList();

        MiningData data = new MiningData(
            items,
            transactions
        );

        System.out.println("Data ok");
        Instant start = Instant.now();

        double minEsupRatio = 0.2;
        int numBuckets = 200; //97

        HashMaxUncertain miner = new HashMaxUncertain(data, minEsupRatio, numBuckets);
        List<Itemset> maximal = miner.run();

        for (Itemset is : maximal) {
            System.out.println(is);
        }
        System.out.println("Found: " + maximal.size() + " itemsets");
        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");
    }
}
