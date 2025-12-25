package org.example;

import org.example.bruteforce.BruteForceSolution;
import org.example.data.DataGenerator;
import org.example.data.Itemset;
import org.example.data.MiningData;
import org.example.data.Transaction;
import org.example.data.Item;
import org.example.data.Unit;
import java.util.List;
import org.example.HashMaxUncertain.HashMaxUncertain;

public class Main2 {
    public static void main(String[] args) {
        // ví dụ tạo dữ liệu nhỏ
        Item A = new Item("A");
        Item B = new Item("B");
        Item C = new Item("C");
        Item D = new Item("D");

        Transaction t1 = new Transaction(List.of(
                new Unit(A, 0.9),
                new Unit(B, 0.8),
                new Unit(C, 0.7)
        ));


        Transaction t2 = new Transaction(List.of(
                new Unit(A, 0.6),
                new Unit(B, 0.6),
                new Unit(C, 0.5)
        ));


        Transaction t3 = new Transaction(List.of(
                new Unit(B, 0.9),
                new Unit(C, 0.8),
                new Unit(D, 0.7)
        ));

        MiningData data = new MiningData(
                List.of(A, B, C, D),
                List.of(t1, t2, t3)
        );

        double minEsupRatio = 0.2;   // ví dụ 50%
        int numBuckets = 97;

        HashMaxUncertain miner = new HashMaxUncertain(data, minEsupRatio, numBuckets);
        List<Itemset> maximal = miner.run();

        for (Itemset is : maximal) {
            System.out.println(is);
        }
    }
}
