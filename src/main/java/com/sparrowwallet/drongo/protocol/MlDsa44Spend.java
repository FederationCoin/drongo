package com.sparrowwallet.drongo.protocol;

/**
 * Single-key ML-DSA-44 witness. Separate from slots and from 87.
 */
public final class MlDsa44Spend {
    private MlDsa44Spend() {
    }

    public static TransactionWitness send(Transaction transaction, byte[] pubkey, byte[] signature) {
        return MlDsaSpend.sendSingleKey(transaction, pubkey, signature);
    }
}
