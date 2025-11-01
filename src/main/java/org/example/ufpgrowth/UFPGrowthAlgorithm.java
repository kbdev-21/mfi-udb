package org.example.ufpgrowth;

import org.example.data.MiningData;

public class UFPGrowthAlgorithm {
    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UFPGrowthAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }
}
