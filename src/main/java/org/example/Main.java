package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.DataGenerator;
import org.example.data.Itemset;
import org.example.data.MiningData;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        int itemsCount = 10;
        int transactionsCount = 1000;
        double minExpectedSupportRate = 0.05;

        MiningData data = DataGenerator.generateRandomData(itemsCount, transactionsCount);

        BruteForceSolution bruteForce = new BruteForceSolution(data, minExpectedSupportRate);
        List<Itemset> maximalFrequentItemsets = bruteForce.maximalFrequentItemsets();
        maximalFrequentItemsets.forEach(System.out::println);
    }
}