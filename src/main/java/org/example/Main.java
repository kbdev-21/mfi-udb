package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.*;
import org.example.ufpgrowth.UFPGrowthAlgorithm;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        int itemsCount = 10;
        int transactionsCount = 2000;
        double minExpectedSupportRate = 0.05;

        System.out.println("Gen data");
        MiningData data = DataGenerator.generateRandomData(itemsCount, transactionsCount);

        System.out.println("algorithm");
        BruteForceSolution bf = new BruteForceSolution(data, minExpectedSupportRate);
        List<Itemset> maximalFrequentItemsets = bf.maximalFrequentItemsets();

        maximalFrequentItemsets.forEach(System.out::println);
    }
}