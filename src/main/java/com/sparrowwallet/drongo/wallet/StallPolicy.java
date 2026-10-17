package com.sparrowwallet.drongo.wallet;

/**
 * Tip older than 3 intervals is a stall. Wallets wait 12 confirmations while that warning is up.
 */
public final class StallPolicy {
    public static final long STALL_TIP_MS = 36L * 60L * 1000L;
    public static final int STALL_CONFIRMATIONS = 12;

    private StallPolicy() {
    }

    public static int requiredConfirmations(long tipAgeMs, int ordinaryConfirmations) {
        return tipAgeMs > STALL_TIP_MS ? STALL_CONFIRMATIONS : ordinaryConfirmations;
    }
}
