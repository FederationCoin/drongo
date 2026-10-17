package com.sparrowwallet.drongo.psbt;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.DilithiumKind;
import com.sparrowwallet.drongo.protocol.SigHash;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionOutput;
import com.sparrowwallet.drongo.protocol.TransactionWitness;
import com.sparrowwallet.drongo.protocol.UnifiedScriptType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Half-signed Dilithium PSBT slots. The kind owns sizes and finalize shape.
 */
public final class DilithiumPsbt {
    private DilithiumPsbt() {
    }

    public static void signInput(PSBT psbt, int inputIndex, DilithiumKind kind, DilithiumKind.Keypair key,
                                 List<TransactionOutput> spent) {
        Transaction tx = psbt.getTransaction();
        byte[] sighash = tx.hashForUnifiedSignature(spent, inputIndex, UnifiedScriptType.WITNESS_V0,
                new byte[0], SigHash.UNIFIED_ALL.value, null, null, null).getBytes();
        byte[] signature = kind.sign(key.secret(), sighash);
        byte[] slot = new byte[kind.pubkeySize() + kind.signatureSize()];
        System.arraycopy(key.pubkey(), 0, slot, 0, key.pubkey().length);
        System.arraycopy(signature, 0, slot, key.pubkey().length, signature.length);
        psbt.getPsbtInputs().get(inputIndex).getProprietary().put(Utils.bytesToHex(kind.keyHash(key.pubkey())), Utils.bytesToHex(slot));
    }

    public static int signedCount(PSBTInput input, DilithiumKind kind, List<byte[]> orderedHashes) {
        int n = 0;
        int signedLen = kind.pubkeySize() + kind.signatureSize();
        for(byte[] hash : orderedHashes) {
            String hex = input.getProprietary().get(Utils.bytesToHex(hash));
            if(hex != null && Utils.hexToBytes(hex).length == signedLen) {
                n++;
            }
        }
        return n;
    }

    public static TransactionWitness finalizeInput(Transaction transaction, PSBTInput input, DilithiumKind kind,
                                                   List<byte[]> orderedHashes) {
        if(!kind.isPolicy()) {
            String hex = input.getProprietary().get(Utils.bytesToHex(orderedHashes.get(0)));
            if(hex == null) {
                throw new IllegalStateException("Dilithium single-key slot is missing");
            }
            byte[] slot = Utils.hexToBytes(hex);
            byte[] pubkey = Arrays.copyOfRange(slot, 0, kind.pubkeySize());
            byte[] signature = Arrays.copyOfRange(slot, kind.pubkeySize(), slot.length);
            return kind.finalizeSingle(transaction, pubkey, signature);
        }
        List<byte[]> slots = new ArrayList<>();
        for(byte[] hash : orderedHashes) {
            String hex = input.getProprietary().get(Utils.bytesToHex(hash));
            if(hex != null) {
                slots.add(Utils.hexToBytes(hex));
            } else {
                slots.add(new byte[0]);
            }
        }
        return kind.finalizePolicy(transaction, slots);
    }
}
