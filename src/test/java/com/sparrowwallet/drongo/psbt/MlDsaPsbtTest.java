package com.sparrowwallet.drongo.psbt;

import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.protocol.Script;
import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionOutput;
import com.sparrowwallet.drongo.protocol.TransactionWitness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MlDsaPsbtTest {
    @Test
    public void twoOfThreeHalfSignedPsbt() throws PSBTParseException {
        byte[] s1 = new byte[32];
        Arrays.fill(s1, (byte)0x01);
        byte[] s2 = new byte[32];
        Arrays.fill(s2, (byte)0x02);
        byte[] s3 = new byte[32];
        Arrays.fill(s3, (byte)0x03);
        MlDsa44.Keypair k1 = MlDsa44.keygen(s1);
        MlDsa44.Keypair k2 = MlDsa44.keygen(s2);
        MlDsa44.Keypair k3 = MlDsa44.keygen(s3);

        List<byte[]> hashes = new ArrayList<>();
        hashes.add(MlDsa44.keyHash(k1.pubkey()));
        hashes.add(MlDsa44.keyHash(k2.pubkey()));
        hashes.add(MlDsa44.keyHash(k3.pubkey()));
        byte[] program = MlDsa44.policyProgram(2, hashes);
        Script script = ScriptType.MLDSA_MULTI.getOutputScript(program);

        Transaction funding = new Transaction();
        funding.addOutput(100_000L, script);
        Transaction spend = new Transaction();
        spend.setVersion(2);
        spend.addInput(funding.getTxId(), 0, new Script(new byte[0]));
        spend.addOutput(90_000L, script);
        List<TransactionOutput> spent = List.of(funding.getOutputs().get(0));

        List<byte[]> ordered = new ArrayList<>(hashes);
        ordered.sort((a, b) -> {
            for(int i = 0; i < 32; i++) {
                int d = (a[i] & 0xff) - (b[i] & 0xff);
                if(d != 0) {
                    return d;
                }
            }
            return 0;
        });

        PSBT wallet1 = new PSBT(spend);
        MlDsaPsbt.signInput(wallet1, 0, k1, spent);
        Assertions.assertEquals(1, MlDsaPsbt.signedCount(wallet1.getPsbtInputs().get(0), ordered));

        TransactionWitness oneSig = MlDsaPsbt.finalizeMultisig(spend, wallet1.getPsbtInputs().get(0), ordered);
        Assertions.assertEquals(1, signedSlots(oneSig.getPushes()));
        Assertions.assertTrue(oneSig.getPushes().stream().anyMatch(p -> p.length == 0));

        PSBT wallet2 = new PSBT(wallet1.serialize());
        MlDsaPsbt.signInput(wallet2, 0, k2, spent);
        Assertions.assertEquals(2, MlDsaPsbt.signedCount(wallet2.getPsbtInputs().get(0), ordered));

        TransactionWitness twoSigs = MlDsaPsbt.finalizeMultisig(spend, wallet2.getPsbtInputs().get(0), ordered);
        Assertions.assertEquals(2, signedSlots(twoSigs.getPushes()));
        Assertions.assertEquals(1, twoSigs.getPushes().stream().filter(p -> p.length == 0).count());
    }

    private static int signedSlots(List<byte[]> stack) {
        int n = 0;
        int signed = MlDsa44.PUBLIC_KEY_SIZE + MlDsa44.SIGNATURE_SIZE;
        for(byte[] push : stack) {
            if(push.length == signed) {
                n++;
            }
        }
        return n;
    }
}
