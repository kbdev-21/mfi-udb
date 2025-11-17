package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.*;
import org.example.uapriori.UAprioriAlgorithm;
import org.example.ufpgrowth.UFPGrowthAlgorithm;

import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        int itemsCount = 10;
        int transactionsCount = 1000;
        double minExpectedSupportRate = 0.1;

        Item a = new Item("A");
        Item b = new Item("B");
        Item c = new Item("C");
        Item d = new Item("D");

        Transaction t1 = new Transaction(List.of(
            new Unit(a, 0.89),
            new Unit(b, 0.83),
            new Unit(d, 0.78)
        ));
        Transaction t2 = new Transaction(List.of(
            new Unit(c, 0.86),
            new Unit(b, 0.76)
        ));
        Transaction t3 = new Transaction(List.of(
            new Unit(a, 0.84),
            new Unit(c, 0.87)
        ));
        Transaction t4 = new Transaction(List.of(
            new Unit(a, 0.78),
            new Unit(b, 0.87),
            new Unit(c, 0.89),
            new Unit(d, 0.94)
        ));

        System.out.println("Gen data");
        MiningData data = DataGenerator.generateRandomData(itemsCount, transactionsCount);
//        MiningData data = new MiningData(List.of(a, b, c, d), List.of(t1, t2, t3, t4));

        var bf = new BruteForceSolution(
            data,
            minExpectedSupportRate
        );
        var bfResult = bf.maximalFrequentItemsets();
        bfResult.forEach(i -> System.out.println(i.toString()));

        System.out.println("apriori");
        UAprioriAlgorithm apriori = new UAprioriAlgorithm(data, minExpectedSupportRate);
        var aprioriResult = apriori.findFrequentItemsets();
        aprioriResult.forEach(i -> System.out.println(i.toString()));

        System.out.println("fp");
        var fp = new UFPGrowthAlgorithm(
            data,
            minExpectedSupportRate
        );
        var fpResult = fp.maximalFrequentItemsets();


    }
}