package org.example.ufpgrowth;

import java.util.List;

public class DiscretizationUtil {
    public static double roundingWithBin(double value, int bin) {
        if (!List.of(2, 4, 5, 10, 20).contains(bin)) {
            throw new RuntimeException("Bin not supported");
        }

        if (value < 0.0) value = 0.0;
        if (value > 1.0) value = 1.0;

        double binSize = 1.0 / bin;
        int binIndex = (int) Math.round(value / binSize);
        double rounded = binIndex * binSize;

        return Math.round(rounded * 100.0) / 100.0;
    }
}
