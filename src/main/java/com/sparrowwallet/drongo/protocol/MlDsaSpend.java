package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.MlDsa44;

import java.util.ArrayList;
import java.util.List;

/**
 * Two send functions. Single-key witness is [pk, sig].
 * Policy witness is n items in output hash order: empty skip or pubkey‖sig.
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
        int signed = MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE;
        for(byte[] slot : slots) {
            if(slot.length == 0) {
                stack.add(new byte[0]);
            } else if(slot.length == signed) {
                stack.add(slot);
            } else {
                throw new ProtocolException("ML-DSA policy witness item length " + slot.length);
            }
        }
        return new TransactionWitness(transaction, stack);
    }
}
