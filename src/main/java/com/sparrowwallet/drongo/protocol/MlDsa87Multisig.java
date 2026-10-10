package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.MlDsa87;

import java.util.ArrayList;
import java.util.List;

/**
 * Slots-only ML-DSA-87 witness. Signed = pk+sig, skipped = 32-byte hash.
 */
public final class MlDsa87Multisig {
    private MlDsa87Multisig() {
    }

    public static TransactionWitness send(Transaction transaction, List<byte[]> slots) {
        List<byte[]> stack = new ArrayList<>();
        int signed = MlDsa87.PUBLIC_KEY_SIZE + MlDsa87.SIGNATURE_SIZE;
        for(byte[] slot : slots) {
            if(slot.length == 32) {
                stack.add(slot);
            } else if(slot.length == signed) {
                byte[] pubkey = new byte[MlDsa87.PUBLIC_KEY_SIZE];
                byte[] signature = new byte[MlDsa87.SIGNATURE_SIZE];
                System.arraycopy(slot, 0, pubkey, 0, pubkey.length);
                System.arraycopy(slot, pubkey.length, signature, 0, signature.length);
                stack.add(pubkey);
                stack.add(signature);
            } else {
                throw new ProtocolException("ML-DSA-87 multisig slot length " + slot.length);
            }
        }
        return new TransactionWitness(transaction, stack);
    }
}
