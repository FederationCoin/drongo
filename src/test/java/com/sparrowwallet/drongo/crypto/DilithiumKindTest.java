package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class DilithiumKindTest {
    @Test
    public void kindsOwnDistinctSeeds() {
        byte[] hd = new byte[32];
        Arrays.fill(hd, (byte)0x42);
        byte[] single87 = MlDsaSeed.derive(hd, DilithiumKind.SINGLE_87.seedKind(), 7);
        byte[] policy87 = MlDsaSeed.derive(hd, DilithiumKind.POLICY_87.seedKind(), MlDsaSeed.policyIndex(7, 0));
        byte[] single44 = MlDsaSeed.derive(hd, DilithiumKind.SINGLE_44.seedKind(), 7);
        Assertions.assertEquals("8d3755b302d8a066e6ae846b00e329cfd597043e812dc509b240876c171e8f68", Utils.bytesToHex(single87));
        Assertions.assertFalse(Arrays.equals(single87, policy87));
        Assertions.assertFalse(Arrays.equals(single87, single44));
    }

    @Test
    public void duplicatePolicyHashesFail() {
        byte[] hash = new byte[32];
        hash[0] = 0x09;
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> DilithiumKind.POLICY_44.policyProgram(1, List.of(hash, hash.clone())));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> DilithiumKind.POLICY_87.policyProgram(1, List.of(hash, hash.clone())));
    }

    @Test
    public void bip32ChildIsNotTheSeed() {
        byte[] hd = new byte[32];
        Arrays.fill(hd, (byte)0x5a);
        byte[] kdf = MlDsaSeed.derive(hd, MlDsaSeed.Kind.DILITHIUM_87, 0);
        Assertions.assertFalse(Arrays.equals(hd, kdf));
        Assertions.assertEquals(32, kdf.length);
    }
}
