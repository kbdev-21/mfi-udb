package org.example;

import org.example.utility.DatasetGenerator;
import org.example.data.MItemset;
import org.example.data.MTransaction;
import org.example.utility.ResultSaver;
import org.example.ugenmax.UGenMax;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class UGenMaxRunner {
    public static void main(String[] args) {
        /* Customizable parameters */
        String datasetFilename = "dataset-mushrooms.txt";
        double minEsupRate = 0.2;
        String resultSavingFilename = "ugenmax-mushrooms-0_2.txt";

        /* Start algorithm */
        Instant start = Instant.now();

        List<MTransaction> dataset = DatasetGenerator.readFromFile("datasets/" + datasetFilename);
        double minEsup = dataset.size() * minEsupRate;

        UGenMax alg = new UGenMax(dataset, minEsup);
        List<MItemset> itemsets = alg.mfi();

        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");

        ResultSaver.saveResultToFile("results/" + resultSavingFilename, itemsets, Duration.between(start, end).toMillis());
    }
}
