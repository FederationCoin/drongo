package com.sparrowwallet.drongo.wallet;

public class StallTxoFilter implements TxoFilter {
    private final Wallet wallet;
    private final long tipAgeMs;
    private final int ordinaryConfirmations;

    public StallTxoFilter(Wallet wallet, long tipAgeMs, int ordinaryConfirmations) {
        this.wallet = wallet;
        this.tipAgeMs = tipAgeMs;
        this.ordinaryConfirmations = ordinaryConfirmations;
    }

    @Override
    public boolean isEligible(BlockTransactionHashIndex candidate) {
        if(wallet.getStoredBlockHeight() == null) {
            return true;
        }
        int need = StallPolicy.requiredConfirmations(tipAgeMs, ordinaryConfirmations);
        return candidate.getConfirmations(wallet.getStoredBlockHeight()) >= need;
    }
}
