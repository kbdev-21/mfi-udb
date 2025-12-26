package org.example.uhashmax;

import org.example.data.Mapper;
import org.example.data.*;
import org.example.data.MTransaction;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UHashMaxRunner {
    private static final Mapper mapper = new Mapper();
    public static void main(String[] args) {
        List<MTransaction> genDataset = DatasetGenerator.readFromFile("datasets/dataset-small1.txt");
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
        int maxK =3;

        UHashMax miner = new UHashMax(data, minEsupRatio, numBuckets,maxK);
        List<Itemset> maximal = miner.run();

        for (Itemset is : maximal) {
            System.out.println(is);
        }
        System.out.println("Found: " + maximal.size() + " itemsets");
        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");
    }
}
