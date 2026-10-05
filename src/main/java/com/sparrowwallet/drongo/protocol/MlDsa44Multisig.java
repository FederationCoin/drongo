package com.sparrowwallet.drongo.protocol;

import java.util.List;

/**
 * Slots-only ML-DSA-44 witness. Separate from single-key and from 87.
 */
public final class MlDsa44Multisig {
    private MlDsa44Multisig() {
    }

    public static TransactionWitness send(Transaction transaction, List<byte[]> slots) {
        return MlDsaSpend.sendMultisig(transaction, slots);
    }
}
