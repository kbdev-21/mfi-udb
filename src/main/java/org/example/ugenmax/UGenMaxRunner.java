package org.example.ugenmax;

import org.example.data.DatasetGenerator;
import org.example.data.MItemset;
import org.example.data.MTransaction;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class UGenMaxRunner {
    public static void main(String[] args) {
        Instant start = Instant.now();
        List<MTransaction> dataset = DatasetGenerator.readFromFile("datasets/dataset-chess.txt");
        double minEsup = dataset.size() * 0.3;
        UGenMax alg = new UGenMax(dataset, minEsup);
        List<MItemset> itemsets = alg.mfi();
        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");
    }
}
