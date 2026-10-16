package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.MlDsa87;

import java.util.ArrayList;
import java.util.List;

/**
 * Policy witness is n items in output hash order: empty skip or pubkey‖sig.
 */
public final class MlDsa87Multisig {
    private MlDsa87Multisig() {
    }

    public static TransactionWitness send(Transaction transaction, List<byte[]> slots) {
        List<byte[]> stack = new ArrayList<>();
        int signed = MlDsa87.PUBLIC_KEY_SIZE + MlDsa87.SIGNATURE_SIZE;
        for(byte[] slot : slots) {
            if(slot.length == 0) {
                stack.add(new byte[0]);
            } else if(slot.length == signed) {
                stack.add(slot);
            } else {
                throw new ProtocolException("ML-DSA-87 policy witness item length " + slot.length);
            }
        }
        return new TransactionWitness(transaction, stack);
    }
}
