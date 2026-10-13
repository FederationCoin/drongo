package com.sparrowwallet.drongo.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SettlementFeeTest {
    @Test
    public void truncatesDown() {
        Assertions.assertEquals(0L, SettlementFee.minimumTokens(0));
        Assertions.assertEquals(0L, SettlementFee.minimumTokens(-1));
        Assertions.assertEquals(0L, SettlementFee.minimumTokens(11));
        Assertions.assertEquals(1L, SettlementFee.minimumTokens(12));
        Assertions.assertEquals(1L, SettlementFee.minimumTokens(23));
        Assertions.assertEquals(13L, SettlementFee.minimumTokens(161));
    }

    @Test
    public void feeMeetsMinimumBranches() {
        Assertions.assertTrue(SettlementFee.feeMeetsMinimum(1, 12));
        Assertions.assertFalse(SettlementFee.feeMeetsMinimum(0, 12));
        Assertions.assertTrue(SettlementFee.feeMeetsMinimum(13, 161));
        Assertions.assertFalse(SettlementFee.feeMeetsMinimum(12, 161));
    }

    @Test
    public void sentenceNamesChainFloorAndTotal() {
        Assertions.assertEquals(
                "The chain has a minimum fee of 1 token per 12 vB, totaling 13 tokens for your transaction.",
                SettlementFee.chainMinimumSentence(161));
    }

    @Test
    public void dustRelayTracksChainMinimum() {
        Assertions.assertEquals(SettlementFee.minimumRateTokensPerVb(), Transaction.DUST_RELAY_TX_FEE);
    }

    @Test
    public void rateIsIntegerFloorOverDivisor() {
        Assertions.assertEquals(1L, SettlementFee.minimumTokens(SettlementFee.VSIZE_DIVISOR));
        Assertions.assertEquals(1.0d / SettlementFee.VSIZE_DIVISOR, SettlementFee.minimumRateTokensPerVb());
    }
}
