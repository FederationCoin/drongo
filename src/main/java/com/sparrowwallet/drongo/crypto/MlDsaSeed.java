package com.sparrowwallet.drongo.crypto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Tagged KDF for Dilithium seeds. Matches node {@code DeriveMlDsaSeed}.
 * A secp BIP32 child is not an input.
 */
public final class MlDsaSeed {
    public static final byte[] TAG = "FederationCoin/ml-dsa-seed/v1".getBytes(StandardCharsets.US_ASCII);
    public static final int MAX_POLICY_KEYS = 12;

    public enum Kind {
        DILITHIUM_44((byte)1),
        DILITHIUM_87((byte)2),
        POLICY_44((byte)3),
        POLICY_87((byte)4);

        private final byte id;

        Kind(byte id) {
            this.id = id;
        }

        public byte id() {
            return id;
        }
    }

    private MlDsaSeed() {
    }

    /**
     * Blake2b-256(tag || seed_material || kind_id || le32(index)).
     * Single-key material is the HD seed or that keystore's root xprv.
     * Policy material is that slot's root xprv, never a wallet-wide HD seed.
     */
    public static byte[] derive(byte[] hdSeed, Kind kind, int index) {
        if(hdSeed == null || hdSeed.length == 0) {
            throw new IllegalArgumentException("HD seed is empty");
        }
        if(kind == null) {
            throw new IllegalArgumentException("kind");
        }
        ByteBuffer buf = ByteBuffer.allocate(TAG.length + hdSeed.length + 1 + 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(TAG);
        buf.put(hdSeed);
        buf.put(kind.id());
        buf.putInt(index);
        return Blake2b256.hash(buf.array());
    }

    public static int policyIndex(int walletIndex, int slot) {
        if(walletIndex < 0 || slot < 0 || slot >= MAX_POLICY_KEYS) {
            throw new IllegalArgumentException("policy KDF index");
        }
        if(walletIndex > (0x7fffffff - (MAX_POLICY_KEYS - 1)) / MAX_POLICY_KEYS) {
            throw new IllegalArgumentException("policy KDF index overflow");
        }
        return walletIndex * MAX_POLICY_KEYS + slot;
    }

    /**
     * Bits 0–30 are the wallet or policy index. Bit 31 is set for change.
     */
    public static int kdfIndex(int pos, boolean change) {
        if(pos < 0) {
            throw new IllegalArgumentException("KDF index");
        }
        return change ? (pos | Integer.MIN_VALUE) : pos;
    }

    public static boolean sameBytes(byte[] left, byte[] right) {
        return Arrays.equals(left, right);
    }
}
