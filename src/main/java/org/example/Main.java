package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.DataGenerator;
import org.example.data.MiningData;

public class Main {
    public static void main(String[] args) {
        int itemsCount = 10;
        int transactionsCount = 1000;
        double minEsupRate = 0.05;

        MiningData data = DataGenerator.generateRandomData(itemsCount, transactionsCount);

        BruteForceSolution bruteForce = new BruteForceSolution(data, minEsupRate);
        bruteForce.execute();
    }
}