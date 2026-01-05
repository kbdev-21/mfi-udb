package org.example.utility;

import org.example.data.MItemset;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ResultSaver {
    public static void saveResultToFile(String filename, List<MItemset> itemsets, long runTime) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            // Row 1: total number of itemsets
            writer.write("Total itemsets found: " + itemsets.size());
            writer.newLine();

            // Row 2: runtime
            writer.write("Elapsed time: " + runTime + "ms");
            writer.newLine();

            // Row 3+: itemsets
            for (MItemset itemset : itemsets) {
                writer.write(itemset.getItems() + " - " + itemset.getExSup());
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save result to file: " + filename, e);
        }
    }
}
