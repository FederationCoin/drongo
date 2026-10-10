package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.MlDsa87;

import java.util.List;

/**
 * Single-key ML-DSA-87 witness [pk, sig].
 */
public final class MlDsa87Spend {
    private MlDsa87Spend() {
    }

    public static TransactionWitness send(Transaction transaction, byte[] pubkey, byte[] signature) {
        if(pubkey.length != MlDsa87.PUBLIC_KEY_SIZE || signature.length != MlDsa87.SIGNATURE_SIZE) {
            throw new ProtocolException("ML-DSA-87 single-key witness lengths");
        }
        return new TransactionWitness(transaction, List.of(pubkey, signature));
    }
}
