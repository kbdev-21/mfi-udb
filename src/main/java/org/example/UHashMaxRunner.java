package org.example;

import org.example.utility.DatasetGenerator;
import org.example.data.MItemset;
import org.example.data.MTransaction;
import org.example.utility.ResultSaver;
import org.example.uhashmax.UHashMax;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class UHashMaxRunner {
    public static void main(String[] args) {
        /* Customizable parameters */
        String datasetFilename = "dataset-mushrooms.txt";
        double minEsupRate = 0.2;
        String resultSavingFilename = "uhashmax-mushrooms-0_2.txt";

        /* Start algorithm */
        Instant start = Instant.now();

        List<MTransaction> dataset = DatasetGenerator.readFromFile("datasets/" + datasetFilename);
        double minEsup = dataset.size() * minEsupRate;

        List<MItemset> itemsets = UHashMax.mfi(dataset, minEsup);

        Instant end = Instant.now();
        System.out.println("Elapsed time: " + Duration.between(start, end).toMillis() + "ms");

        ResultSaver.saveResultToFile("results/" + resultSavingFilename, itemsets, Duration.between(start, end).toMillis());
    }
}
