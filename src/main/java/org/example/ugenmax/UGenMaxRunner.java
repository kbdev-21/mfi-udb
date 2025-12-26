package org.example.ugenmax;

import org.example.data.DatasetGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class UGenMaxRunner {
    public static void main(String[] args) {
        Instant start = Instant.now();
        List<MTransaction> dataset = DatasetGenerator.readFromFile("datasets/dataset-small.txt");
        double minEsup = dataset.size() * 0.2;
        UGenMax alg = new UGenMax(dataset, minEsup);
        List<MItemset> itemsets = alg.mfi();
        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");
    }
}
