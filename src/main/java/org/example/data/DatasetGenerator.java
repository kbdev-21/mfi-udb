package org.example.data;

import java.io.*;
import java.util.*;

public class DatasetGenerator {
    public static void main(String[] args) {
        List<MTransaction> newDataset = DatasetGenerator.generateRandomDataset(100, 50000);
        //String filename = "dataset-" + UUID.randomUUID() + ".txt";
        String filename = "dataset-large.txt";
        DatasetGenerator.writeToFile(newDataset, "datasets/" + filename);
    }

    private static List<String> generateItems(int numOfItems) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < numOfItems; i++) {
            items.add("I" + i);
        }
        return items;
    }

    public static List<MTransaction> generateRandomDataset(
        int numOfItems,
        int numOfTransactions
    ) {
        List<MTransaction> dataset = new ArrayList<>();
        Random random = new Random();

        List<String> items = generateItems(numOfItems);

        for (int t = 1; t <= numOfTransactions; t++) {
            LinkedHashMap<String, Double> units = new LinkedHashMap<>();

            int itemsInTransaction = 1 + random.nextInt(numOfItems);
            //int itemsInTransaction = numOfItems / 2 + random.nextInt(numOfItems - numOfItems / 2 + 1);
            Collections.shuffle(items, random);

            for (int i = 0; i < itemsInTransaction; i++) {
                String item = items.get(i);
                double prob = 0.8 + random.nextDouble() * 0.2;
                prob = Math.round(prob * 100.0) / 100.0;
                units.put(item, prob);
            }

            dataset.add(new MTransaction("T" + t, units));
        }

        return dataset;
    }

    /* ================= GHI FILE TXT ================= */

    public static void writeToFile(
        List<MTransaction> dataset,
        String filePath
    ) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            for (MTransaction t : dataset) {
                StringBuilder sb = new StringBuilder();
                sb.append(t.getId()).append(": ");

                Iterator<Map.Entry<String, Double>> it =
                    t.getUnits().entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<String, Double> e = it.next();
                    sb.append(e.getKey())
                        .append("(")
                        .append(e.getValue())
                        .append(")");

                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }

                writer.write(sb.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing dataset to file", e);
        }
    }

    public static List<MTransaction> readFromFile(String filePath) {
        List<MTransaction> dataset = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // T1: I0(0.82), I3(0.67)
                String[] parts = line.split(":");
                String tid = parts[0].trim();
                String itemsPart = parts[1].trim();

                LinkedHashMap<String, Double> units = new LinkedHashMap<>();

                String[] itemTokens = itemsPart.split(", ");
                for (String token : itemTokens) {
                    // I0(0.82)
                    int l = token.indexOf('(');
                    int r = token.indexOf(')');

                    String item = token.substring(0, l);
                    double prob = Double.parseDouble(
                        token.substring(l + 1, r)
                    );

                    units.put(item, prob);
                }

                dataset.add(new MTransaction(tid, units));
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading dataset file", e);
        }

        return dataset;
    }
}
