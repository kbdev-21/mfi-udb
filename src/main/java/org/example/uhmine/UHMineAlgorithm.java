package org.example.uhmine;

import org.example.data.MiningData;

public class UHMineAlgorithm {
    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UHMineAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }
}
