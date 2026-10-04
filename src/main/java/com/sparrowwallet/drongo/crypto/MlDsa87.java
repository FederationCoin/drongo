package com.sparrowwallet.drongo.crypto;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.ParametersWithContext;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSASigner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ML-DSA-87 with context FederationCoin. Matches the node mldsa87-tool.
 */
public final class MlDsa87 {
    public static final int PUBLIC_KEY_SIZE = 2592;
    public static final int SIGNATURE_SIZE = 4627;
    public static final int SEED_SIZE = 32;
    public static final int SECRET_KEY_SIZE = 4896;
    public static final int MAX_KEYS = 12;
    public static final byte[] CONTEXT = "FederationCoin".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_TAG = "FCN-MLDSA87-KEY".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] POLICY_TAG = "FCN-MLDSA87-POLICY".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] CHILD_TAG = "FCN-MLDSA87-CHILD".getBytes(StandardCharsets.US_ASCII);

    public record Keypair(byte[] pubkey, byte[] secret) {}

    private MlDsa87() {
    }

    public static byte[] blake2b256(byte[] data) {
        Blake2bDigest digest = new Blake2bDigest(256);
        digest.update(data, 0, data.length);
        byte[] out = new byte[32];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] childSeed(byte[] master, int index) {
        ByteBuffer buf = ByteBuffer.allocate(1 + CHILD_TAG.length + master.length + 4);
        buf.put((byte)0x11);
        buf.put(CHILD_TAG);
        buf.put(master);
        buf.putInt(index);
        return blake2b256(buf.array());
    }

    public static Keypair keygen(byte[] seed) {
        if(seed.length != SEED_SIZE) {
            throw new IllegalArgumentException("ML-DSA-87 seed must be 32 bytes");
        }
        MLDSAPrivateKeyParameters priv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_87, seed);
        return new Keypair(priv.getPublicKey(), priv.getEncoded());
    }

    public static byte[] sign(byte[] secret, byte[] message) {
        MLDSAPrivateKeyParameters priv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_87, secret);
        MLDSASigner signer = new MLDSASigner();
        signer.init(true, new ParametersWithContext(new ParametersWithRandom(priv, zeros()), CONTEXT));
        signer.update(message, 0, message.length);
        try {
            byte[] signature = signer.generateSignature();
            if(signature.length != SIGNATURE_SIZE) {
                throw new IllegalStateException("ML-DSA-87 signature length " + signature.length);
            }
            return signature;
        } catch(CryptoException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean verify(byte[] pubkey, byte[] signature, byte[] message) {
        if(pubkey.length != PUBLIC_KEY_SIZE || signature.length != SIGNATURE_SIZE) {
            return false;
        }
        MLDSAPublicKeyParameters pub = new MLDSAPublicKeyParameters(MLDSAParameters.ml_dsa_87, pubkey);
        MLDSASigner signer = new MLDSASigner();
        signer.init(false, new ParametersWithContext(pub, CONTEXT));
        signer.update(message, 0, message.length);
        return signer.verifySignature(signature);
    }

    public static byte[] keyHash(byte[] pubkey) {
        ByteBuffer buf = ByteBuffer.allocate(1 + KEY_TAG.length + pubkey.length);
        buf.put((byte)0x0f);
        buf.put(KEY_TAG);
        buf.put(pubkey);
        return blake2b256(buf.array());
    }

    public static byte[] policyProgram(int threshold, List<byte[]> keyHashes) {
        if(keyHashes.size() < 1 || keyHashes.size() > MAX_KEYS) {
            throw new IllegalArgumentException("ML-DSA-87 policy key count");
        }
        if(threshold < 1 || threshold > keyHashes.size()) {
            throw new IllegalArgumentException("ML-DSA-87 policy threshold");
        }
        List<byte[]> ordered = new ArrayList<>(keyHashes);
        ordered.sort((a, b) -> {
            for(int i = 0; i < 32; i++) {
                int d = (a[i] & 0xff) - (b[i] & 0xff);
                if(d != 0) {
                    return d;
                }
            }
            return 0;
        });
        ByteBuffer buf = ByteBuffer.allocate(1 + POLICY_TAG.length + 2 + ordered.size() * 32);
        buf.put((byte)0x12);
        buf.put(POLICY_TAG);
        buf.put((byte)threshold);
        buf.put((byte)ordered.size());
        for(byte[] hash : ordered) {
            buf.put(hash);
        }
        return blake2b256(buf.array());
    }

    private static SecureRandom zeros() {
        return new SecureRandom() {
            @Override
            public void nextBytes(byte[] bytes) {
                Arrays.fill(bytes, (byte)0);
            }
        };
    }
}
