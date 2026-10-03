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
        int signed = MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE;
        for(byte[] slot : slots) {
            if(slot.length == 32) {
                stack.add(slot);
            } else if(slot.length == signed) {
                byte[] pubkey = new byte[MlDsa44.PUBLIC_KEY_SIZE];
                byte[] signature = new byte[MlDsa44.SIGNATURE_SIZE];
                System.arraycopy(slot, 0, pubkey, 0, pubkey.length);
                System.arraycopy(slot, pubkey.length, signature, 0, signature.length);
                stack.add(pubkey);
                stack.add(signature);
            } else {
                throw new ProtocolException("ML-DSA multisig slot length " + slot.length);
            }
        }
        return new TransactionWitness(transaction, stack);
    }
}
