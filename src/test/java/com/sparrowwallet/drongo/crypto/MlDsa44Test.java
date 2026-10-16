package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.wallet.StallPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MlDsa44Test {
    @Test
    public void keygenMatchesNodeTool() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        MlDsa44.Keypair key = MlDsa44.keygen(seed);
        Assertions.assertEquals(1312, key.pubkey().length);
        Assertions.assertEquals(2560, key.secret().length);
        Assertions.assertEquals("54c981c4059222f4f9b4a406a13230cd2786ab8a3e026d5700bedeb2b3b64c82",
                Utils.bytesToHex(MlDsa44.keyHash(key.pubkey())));
        byte[] message = new byte[32];
        Arrays.fill(message, (byte)0xaa);
        byte[] signature = MlDsa44.sign(key.secret(), message);
        Assertions.assertEquals(2420, signature.length);
        Assertions.assertEquals("5793ddede5ba766ada8ff8366b720ddf", Utils.bytesToHex(Arrays.copyOf(signature, 16)));
        Assertions.assertTrue(MlDsa44.verify(key.pubkey(), signature, message));
    }

    @Test
    public void stallWaitsTwelve() {
        Assertions.assertEquals(6, StallPolicy.requiredConfirmations(StallPolicy.STALL_TIP_MS, 6));
        Assertions.assertEquals(12, StallPolicy.requiredConfirmations(StallPolicy.STALL_TIP_MS + 1, 6));
    }

    @Test
    public void keygenRejectsWrongSeedLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.keygen(new byte[31]));
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.keygen(new byte[33]));
    }

    @Test
    public void verifyRejectsSizeMismatch() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        MlDsa44.Keypair key = MlDsa44.keygen(seed);
        byte[] message = new byte[32];
        byte[] signature = MlDsa44.sign(key.secret(), message);
        Assertions.assertFalse(MlDsa44.verify(new byte[key.pubkey().length - 1], signature, message));
        Assertions.assertFalse(MlDsa44.verify(key.pubkey(), new byte[signature.length - 1], message));
        Assertions.assertTrue(MlDsa44.verify(key.pubkey(), signature, message));
    }

    @Test
    public void policyProgramRejectsCountThresholdHashAndDuplicates() {
        byte[] a = new byte[32];
        a[0] = 0x01;
        byte[] b = new byte[32];
        b[0] = 0x02;
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.policyProgram(1, java.util.List.of()));
        java.util.List<byte[]> tooMany = new java.util.ArrayList<>();
        for(int i = 0; i < MlDsa44.MAX_KEYS + 1; i++) {
            byte[] hash = new byte[32];
            hash[0] = (byte)(i + 1);
            tooMany.add(hash);
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.policyProgram(1, tooMany));
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.policyProgram(0, java.util.List.of(a)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.policyProgram(2, java.util.List.of(a)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> MlDsa44.policyProgram(1, java.util.List.of(new byte[31])));
        IllegalArgumentException duplicate = Assertions.assertThrows(IllegalArgumentException.class,
                () -> MlDsa44.policyProgram(1, java.util.List.of(a, a.clone())));
        Assertions.assertTrue(duplicate.getMessage().contains("strictly increasing"));
        Assertions.assertEquals(MlDsa44.policyProgram(1, java.util.List.of(b, a)).length,
                MlDsa44.policyProgram(1, java.util.List.of(a, b)).length);
    }
}
