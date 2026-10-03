package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.MlDsa44;

import java.util.ArrayList;
import java.util.List;

/**
 * Two send functions. Single-key witness is [pk, sig].
 * Multisig is one slot per committed key, lex order: signed = pk+sig, skipped = 32-byte hash.
 */
public final class MlDsaSpend {
    private MlDsaSpend() {
    }

    public static TransactionWitness sendSingleKey(Transaction transaction, byte[] pubkey, byte[] signature) {
        if(pubkey.length != MlDsa44.PUBLIC_KEY_SIZE || signature.length != MlDsa44.SIGNATURE_SIZE) {
            throw new ProtocolException("ML-DSA single-key witness lengths");
        }
        return new TransactionWitness(transaction, List.of(pubkey, signature));
    }

    public static TransactionWitness sendMultisig(Transaction transaction, List<byte[]> slots) {
        List<byte[]> stack = new ArrayList<>();
        for(byte[] slot : slots) {
            if(slot.length == 32 || slot.length == MlDsa44.PUBLIC_KEY_SIZE || slot.length == MlDsa44.SIGNATURE_SIZE) {
                stack.add(slot);
            } else {
                throw new ProtocolException("ML-DSA multisig slot length " + slot.length);
            }
        }
        return new TransactionWitness(transaction, stack);
    }
}
