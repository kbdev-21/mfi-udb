package org.example.ufpgrowth;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.*;

import java.util.List;

public class TestUFPGrowth {
    public static void main(String[] args) {
        System.out.println("TestUFPTree");

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

        List<Transaction> transactions = List.of(t1, t2, t3, t4);
//        transactions.forEach(transaction -> {
//            transaction.getUnits().forEach(unit -> {
//                System.out.println(DiscretizationUtil.roundingWithBin(unit.getProbability(), 20));
//            });
//        });

        int itemsCount = 20;
        int transactionsCount = 10000;
        double minExpectedSupportRate = 0.05;
        var data = DataGenerator.generateRandomData(itemsCount, transactionsCount);
        //var data = new MiningData(List.of(a,b,c,d), List.of(t1,t2,t3,t4));

//        var bf = new BruteForceSolution(
//            data,
//            minExpectedSupportRate
//        );
//        var bfResult = bf.maximalFrequentItemsets();
//        bfResult.forEach(i -> System.out.println(i.toString()));

        var ufp = new UFPGrowthAlgorithm(
            data,
            minExpectedSupportRate
        );

        var ufpResult = ufp.maximalFrequentItemsets();
        ufpResult.forEach(i -> System.out.println(i.toString()));
    }


}
