package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.psbt.MlDsaPsbt;
import com.sparrowwallet.drongo.psbt.PSBT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MlDsaSpendTest {
    @Test
    public void twoOfThreeHalfSignedPsbt() throws Exception {
        MlDsa44.Keypair[] keys = new MlDsa44.Keypair[3];
        List<byte[]> hashes = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            byte[] seed = new byte[32];
            Arrays.fill(seed, (byte)(0x21 + i));
            keys[i] = MlDsa44.keygen(MlDsa44.childSeed(seed, 0));
            hashes.add(MlDsa44.keyHash(keys[i].pubkey()));
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
        byte[] program = MlDsa44.policyProgram(2, hashes);

        Transaction funding = new Transaction();
        funding.addOutput(100_000_000L, ScriptType.MLDSA_MULTI.getOutputScript(program));
        TransactionOutput utxo = funding.getOutputs().get(0);

        Transaction spend = new Transaction();
        spend.addInput(funding.getTxId(), 0, new Script(new byte[0]));
        spend.addOutput(90_000_000L, ScriptType.MLDSA_MULTI.getOutputScript(program));

        PSBT first = new PSBT(spend);
        first.getPsbtInputs().get(0).setWitnessUtxo(utxo);
        MlDsa44.Keypair firstKey = keyForHash(keys, hashes.get(0));
        MlDsaPsbt.signInput(first, 0, firstKey, List.of(utxo));
        Assertions.assertEquals(1, MlDsaPsbt.signedCount(first.getPsbtInputs().get(0), hashes));
        TransactionWitness oneSig = MlDsaPsbt.finalizeMultisig(spend, first.getPsbtInputs().get(0), hashes);
        Assertions.assertTrue(oneSig.getPushes().stream().anyMatch(p -> p.length == 32));
        Assertions.assertEquals(1, countSignedSlots(oneSig));

        PSBT second = new PSBT(first.serialize(), false);
        second.getPsbtInputs().get(0).setWitnessUtxo(utxo);
        MlDsa44.Keypair secondKey = keyForHash(keys, hashes.get(1));
        MlDsaPsbt.signInput(second, 0, secondKey, List.of(utxo));
        Assertions.assertEquals(2, MlDsaPsbt.signedCount(second.getPsbtInputs().get(0), hashes));
        TransactionWitness twoSig = MlDsaPsbt.finalizeMultisig(spend, second.getPsbtInputs().get(0), hashes);
        Assertions.assertEquals(2, countSignedSlots(twoSig));
        Assertions.assertEquals(1, twoSig.getPushes().stream().filter(p -> p.length == 32).count());
        Assertions.assertEquals(Utils.bytesToHex(hashes.get(2)), Utils.bytesToHex(skipHash(twoSig)));
        byte[] sighash = spend.hashForUnifiedSignature(List.of(utxo), 0, UnifiedScriptType.WITNESS_V0,
                utxo.getScript().getProgram(), SigHash.UNIFIED_ALL.value, null, null, null).getBytes();
        Assertions.assertTrue(MlDsa44.verify(firstKey.pubkey(), signedSignature(twoSig, 0), sighash));
        Assertions.assertTrue(MlDsa44.verify(secondKey.pubkey(), signedSignature(twoSig, 1), sighash));
    }

    private static MlDsa44.Keypair keyForHash(MlDsa44.Keypair[] keys, byte[] hash) {
        for(MlDsa44.Keypair key : keys) {
            if(Arrays.equals(MlDsa44.keyHash(key.pubkey()), hash)) {
                return key;
            }
        }
        throw new IllegalStateException("missing key");
    }

    private static int countSignedSlots(TransactionWitness witness) {
        int n = 0;
        List<byte[]> pushes = witness.getPushes();
        for(int i = 0; i < pushes.size(); ) {
            if(pushes.get(i).length == 32) {
                i++;
            } else if(pushes.get(i).length == MlDsa44.PUBLIC_KEY_SIZE) {
                n++;
                i += 2;
            } else {
                i++;
            }
        }
        return n;
    }

    private static byte[] skipHash(TransactionWitness witness) {
        for(byte[] push : witness.getPushes()) {
            if(push.length == 32) {
                return push;
            }
        }
        throw new IllegalStateException("no skip slot");
    }

    private static byte[] signedSignature(TransactionWitness witness, int signedIndex) {
        int seen = 0;
        List<byte[]> pushes = witness.getPushes();
        for(int i = 0; i < pushes.size(); ) {
            if(pushes.get(i).length == 32) {
                i++;
            } else {
                if(seen == signedIndex) {
                    return pushes.get(i + 1);
                }
                seen++;
                i += 2;
            }
        }
        throw new IllegalStateException("no signed slot");
    }
}
