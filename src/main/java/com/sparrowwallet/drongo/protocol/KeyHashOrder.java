package com.sparrowwallet.drongo.protocol;

import java.util.Comparator;

/**
 * memcmp of the stored 32 bytes. Same order as node uint256 operator&lt;.
 * Do not sort by hex display.
 */
public final class KeyHashOrder implements Comparator<byte[]> {
    public static final KeyHashOrder INSTANCE = new KeyHashOrder();

    private KeyHashOrder() {
    }

    @Override
    public int compare(byte[] a, byte[] b) {
        if(a == null || b == null || a.length != 32 || b.length != 32) {
            throw new IllegalArgumentException("ML-DSA policy hash must be 32 bytes");
        }
        for(int i = 0; i < 32; i++) {
            int d = (a[i] & 0xff) - (b[i] & 0xff);
            if(d != 0) {
                return d;
            }
        }
        return 0;
    }
}
