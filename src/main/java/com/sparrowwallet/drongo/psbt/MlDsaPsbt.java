package com.sparrowwallet.drongo.psbt;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.protocol.MlDsaSpend;
import com.sparrowwallet.drongo.protocol.SigHash;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionOutput;
import com.sparrowwallet.drongo.protocol.TransactionWitness;
import com.sparrowwallet.drongo.protocol.UnifiedScriptType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Half-signed ML-DSA PSBT slots. Signed = pubkey||sig. Skip = 32-byte key hash.
 */
public final class MlDsaPsbt {
    private MlDsaPsbt() {
    }

    public static void signInput(PSBT psbt, int inputIndex, MlDsa44.Keypair key, List<TransactionOutput> spent) {
        Transaction tx = psbt.getTransaction();
        byte[] script = spent.get(inputIndex).getScript().getProgram();
        byte[] sighash = tx.hashForUnifiedSignature(spent, inputIndex, UnifiedScriptType.WITNESS_V0,
                script, SigHash.UNIFIED_ALL.value, null, null, null).getBytes();
        byte[] signature = MlDsa44.sign(key.secret(), sighash);
        byte[] slot = new byte[MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE];
        System.arraycopy(key.pubkey(), 0, slot, 0, key.pubkey().length);
        System.arraycopy(signature, 0, slot, key.pubkey().length, signature.length);
        psbt.getPsbtInputs().get(inputIndex).getProprietary().put(Utils.bytesToHex(MlDsa44.keyHash(key.pubkey())), Utils.bytesToHex(slot));
    }

    public static int signedCount(PSBTInput input, List<byte[]> orderedHashes) {
        int n = 0;
        for(byte[] hash : orderedHashes) {
            String hex = input.getProprietary().get(Utils.bytesToHex(hash));
            if(hex != null && Utils.hexToBytes(hex).length == MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE) {
                n++;
            }
        }
        return n;
    }

    public static TransactionWitness finalizeMultisig(Transaction transaction, PSBTInput input, List<byte[]> orderedHashes) {
        List<byte[]> slots = new ArrayList<>();
        for(byte[] hash : orderedHashes) {
            String hex = input.getProprietary().get(Utils.bytesToHex(hash));
            if(hex != null) {
                slots.add(Utils.hexToBytes(hex));
            } else {
                slots.add(Arrays.copyOf(hash, 32));
            }
        }
        return MlDsaSpend.sendMultisig(transaction, slots);
    }

    public static TransactionWitness finalizeSingle(Transaction transaction, PSBTInput input, byte[] keyHash) {
        String hex = input.getProprietary().get(Utils.bytesToHex(keyHash));
        if(hex == null) {
            throw new IllegalStateException("ML-DSA single-key slot is missing");
        }
        byte[] slot = Utils.hexToBytes(hex);
        byte[] pubkey = Arrays.copyOfRange(slot, 0, MlDsa44.PUBLIC_KEY_SIZE);
        byte[] signature = Arrays.copyOfRange(slot, MlDsa44.PUBLIC_KEY_SIZE, slot.length);
        return MlDsaSpend.sendSingleKey(transaction, pubkey, signature);
    }
}
