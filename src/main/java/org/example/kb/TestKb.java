package org.example.kb;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TestKb {
    public static void main(String[] args) {
        Instant start = Instant.now();
        List<MTransaction> dataset = GenDataset.readFromFile("data/dataset-small.txt");
        double minEsup = dataset.size() * 0.2;
        UGenMax alg = new UGenMax(dataset, minEsup);
        List<MItemset> itemsets = alg.mfi();
        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");
    }
}
