package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.MlDsa87;
import com.sparrowwallet.drongo.psbt.MlDsa87Psbt;
import com.sparrowwallet.drongo.psbt.PSBT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MlDsa87SpendTest {
    @Test
    public void twoOfThreeHalfSignedPsbt() throws Exception {
        MlDsa87.Keypair[] keys = new MlDsa87.Keypair[3];
        List<byte[]> hashes = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            byte[] seed = new byte[32];
            Arrays.fill(seed, (byte)(0x41 + i));
            keys[i] = MlDsa87.keygen(seed);
            hashes.add(MlDsa87.keyHash(keys[i].pubkey()));
        }
        hashes.sort((a, b) -> {
            for(int i = 0; i < 32; i++) {
                int d = (a[i] & 0xff) - (b[i] & 0xff);
                if(d != 0) {
                    return d;
                }
            }
            return 0;
        });
        byte[] program = MlDsa87.policyProgram(2, hashes);

        Transaction funding = new Transaction();
        funding.addOutput(100_000_000L, ScriptType.MLDSA87_MULTI.getOutputScript(program));
        TransactionOutput utxo = funding.getOutputs().get(0);

        Transaction spend = new Transaction();
        spend.addInput(funding.getTxId(), 0, new Script(new byte[0]));
        spend.addOutput(90_000_000L, ScriptType.MLDSA87_MULTI.getOutputScript(program));

        PSBT first = new PSBT(spend);
        first.getPsbtInputs().get(0).setWitnessUtxo(utxo);
        MlDsa87.Keypair firstKey = keyForHash(keys, hashes.get(0));
        MlDsa87Psbt.signInput(first, 0, firstKey, List.of(utxo));
        Assertions.assertEquals(1, MlDsa87Psbt.signedCount(first.getPsbtInputs().get(0), hashes));
        TransactionWitness oneSig = MlDsa87Psbt.finalizeMultisig(spend, first.getPsbtInputs().get(0), hashes);
        Assertions.assertEquals(1, countSignedSlots(oneSig));

        PSBT second = new PSBT(first.serialize(), false);
        second.getPsbtInputs().get(0).setWitnessUtxo(utxo);
        MlDsa87.Keypair secondKey = keyForHash(keys, hashes.get(1));
        MlDsa87Psbt.signInput(second, 0, secondKey, List.of(utxo));
        Assertions.assertEquals(2, MlDsa87Psbt.signedCount(second.getPsbtInputs().get(0), hashes));
        TransactionWitness twoSig = MlDsa87Psbt.finalizeMultisig(spend, second.getPsbtInputs().get(0), hashes);
        Assertions.assertEquals(2, countSignedSlots(twoSig));
        Assertions.assertEquals(1, twoSig.getPushes().stream().filter(p -> p.length == 0).count());
        byte[] sighash = spend.hashForUnifiedSignature(List.of(utxo), 0, UnifiedScriptType.WITNESS_V0,
                new byte[0], SigHash.UNIFIED_ALL.value, null, null, null).getBytes();
        Assertions.assertTrue(MlDsa87.verify(firstKey.pubkey(), signedSignature(twoSig, 0), sighash));
        Assertions.assertTrue(MlDsa87.verify(secondKey.pubkey(), signedSignature(twoSig, 1), sighash));
    }

    private static MlDsa87.Keypair keyForHash(MlDsa87.Keypair[] keys, byte[] hash) {
        for(MlDsa87.Keypair key : keys) {
            if(Arrays.equals(MlDsa87.keyHash(key.pubkey()), hash)) {
                return key;
            }
        }
        throw new IllegalStateException("missing key");
    }

    private static int countSignedSlots(TransactionWitness witness) {
        int n = 0;
        int signed = MlDsa87.PUBLIC_KEY_SIZE + MlDsa87.SIGNATURE_SIZE;
        for(byte[] push : witness.getPushes()) {
            if(push.length == signed) {
                n++;
            }
        }
        return n;
    }

    private static byte[] signedSignature(TransactionWitness witness, int signedIndex) {
        int seen = 0;
        int signed = MlDsa87.PUBLIC_KEY_SIZE + MlDsa87.SIGNATURE_SIZE;
        for(byte[] push : witness.getPushes()) {
            if(push.length != signed) {
                continue;
            }
            if(seen == signedIndex) {
                return Arrays.copyOfRange(push, MlDsa87.PUBLIC_KEY_SIZE, push.length);
            }
            seen++;
        }
        throw new IllegalStateException("no signed slot");
    }
}
