package com.sparrowwallet.drongo.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KeyHashOrderTest {
    @Test
    public void memcmpStoredBytesNotHex() {
        byte[] lo = new byte[32];
        byte[] hi = new byte[32];
        lo[0] = 0x00;
        hi[0] = (byte)0xff;
        Assertions.assertTrue(KeyHashOrder.INSTANCE.compare(lo, hi) < 0);
        Assertions.assertTrue(KeyHashOrder.INSTANCE.compare(hi, lo) > 0);
        Assertions.assertEquals(0, KeyHashOrder.INSTANCE.compare(lo, lo.clone()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> KeyHashOrder.INSTANCE.compare(new byte[31], hi));
    }
}
