package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class MlDsa87Test {
    @Test
    public void keygenRoundTrip() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x22);
        MlDsa87.Keypair key = MlDsa87.keygen(seed);
        Assertions.assertEquals(2592, key.pubkey().length);
        Assertions.assertEquals(4896, key.secret().length);
        byte[] message = new byte[32];
        Arrays.fill(message, (byte)0xaa);
        byte[] signature = MlDsa87.sign(key.secret(), message);
        Assertions.assertEquals(4627, signature.length);
        Assertions.assertTrue(MlDsa87.verify(key.pubkey(), signature, message));
        Assertions.assertEquals(32, MlDsa87.keyHash(key.pubkey()).length);
        byte[] a = MlDsa87.keyHash(key.pubkey());
        byte[] otherSeed = new byte[32];
        Arrays.fill(otherSeed, (byte)0x23);
        byte[] b = MlDsa87.keyHash(MlDsa87.keygen(otherSeed).pubkey());
        Assertions.assertEquals(71, MlDsa87.policyProgram(2, List.of(a, b)).length);
        IllegalArgumentException equal = Assertions.assertThrows(IllegalArgumentException.class,
                () -> MlDsa87.policyProgram(1, List.of(a, a.clone())));
        Assertions.assertTrue(equal.getMessage().contains("strictly increasing"));
        IllegalArgumentException shortHash = Assertions.assertThrows(IllegalArgumentException.class,
                () -> MlDsa87.policyProgram(1, List.of(new byte[31])));
        Assertions.assertTrue(shortHash.getMessage().contains("32"));
        Assertions.assertNotEquals(Utils.bytesToHex(a), Utils.bytesToHex(MlDsa44.keyHash(MlDsa44.keygen(seed).pubkey())));
    }
}
