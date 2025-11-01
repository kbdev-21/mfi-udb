package org.example.uapriori;

import org.example.data.MiningData;

public class UAprioriAlgorithm {
    private final MiningData miningData;
    private final double minExpectedSupportRate;

    public UAprioriAlgorithm(MiningData miningData, double minExpectedSupportRate) {
        this.miningData = miningData;
        this.minExpectedSupportRate = minExpectedSupportRate;
    }
}
