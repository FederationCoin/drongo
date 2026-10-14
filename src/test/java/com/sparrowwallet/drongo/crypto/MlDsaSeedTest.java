package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MlDsaSeedTest {
    @Test
    public void pinnedVectorMatchesNode() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x42);
        byte[] out = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, 7);
        Assertions.assertEquals("8d3755b302d8a066e6ae846b00e329cfd597043e812dc509b240876c171e8f68", Utils.bytesToHex(out));
    }

    @Test
    public void kindAndIndexDiverge() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        byte[] a = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_44, 0);
        byte[] b = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, 0);
        byte[] c = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_44, 1);
        Assertions.assertFalse(Arrays.equals(a, b));
        Assertions.assertFalse(Arrays.equals(a, c));
        Assertions.assertFalse(Arrays.equals(b, c));
    }

    @Test
    public void emptySeedFails() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> MlDsaSeed.derive(new byte[0], MlDsaSeed.Kind.DILITHIUM_44, 0));
    }

    @Test
    public void changeBitDiverges() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        byte[] recv = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, MlDsaSeed.kdfIndex(0, false));
        byte[] change = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, MlDsaSeed.kdfIndex(0, true));
        Assertions.assertEquals(0, MlDsaSeed.kdfIndex(0, false));
        Assertions.assertEquals(Integer.MIN_VALUE, MlDsaSeed.kdfIndex(0, true));
        Assertions.assertFalse(Arrays.equals(recv, change));
    }
}
