package com.sparrowwallet.drongo.protocol;

public final class SettlementFee {
    public static final int VSIZE_DIVISOR = 12;

    private SettlementFee() {
    }

    public static long minimumTokens(long virtualBytes) {
        return virtualBytes <= 0 ? 0 : virtualBytes / VSIZE_DIVISOR;
    }

    /**
     * Tokens per virtual byte at the chain floor. Same rule as {@link #minimumTokens(long)}:
     * 1 token per {@link #VSIZE_DIVISOR} vB, as a rate for fee-rate maps.
     */
    public static double minimumRateTokensPerVb() {
        return (double) minimumTokens(VSIZE_DIVISOR) / (double) VSIZE_DIVISOR;
    }

    public static boolean feeMeetsMinimum(long feeTokens, long virtualBytes) {
        return feeTokens >= minimumTokens(virtualBytes);
    }

    public static String chainMinimumSentence(long virtualBytes) {
        return "The chain has a minimum fee of 1 token per 12 vB, totaling "
                + minimumTokens(virtualBytes)
                + " tokens for your transaction.";
    }
}
