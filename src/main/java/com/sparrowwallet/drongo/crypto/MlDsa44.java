package com.sparrowwallet.drongo.crypto;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.params.ParametersWithContext;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPublicKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSASigner;
import com.sparrowwallet.drongo.protocol.KeyHashOrder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ML-DSA-44 with context FederationCoin. Matches the node mldsa44-tool.
 */
public final class MlDsa44 {
    public static final int PUBLIC_KEY_SIZE = 1312;
    public static final int SIGNATURE_SIZE = 2420;
    public static final int SEED_SIZE = 32;
    public static final int SECRET_KEY_SIZE = 2560;
    public static final int MAX_KEYS = 12;
    public static final byte[] CONTEXT = "FederationCoin".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] KEY_TAG = "FCN-MLDSA44-KEY".getBytes(StandardCharsets.US_ASCII);

    public record Keypair(byte[] pubkey, byte[] secret) {}

    private MlDsa44() {
    }

    public static byte[] blake2b256(byte[] data) {
        Blake2bDigest digest = new Blake2bDigest(256);
        digest.update(data, 0, data.length);
        byte[] out = new byte[32];
        digest.doFinal(out, 0);
        return out;
    }

    public static Keypair keygen(byte[] seed) {
        if(seed.length != SEED_SIZE) {
            throw new IllegalArgumentException("ML-DSA-44 seed must be 32 bytes");
        }
        MLDSAPrivateKeyParameters priv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_44, seed);
        return new Keypair(priv.getPublicKey(), priv.getEncoded());
    }

    public static byte[] sign(byte[] secret, byte[] message) {
        MLDSAPrivateKeyParameters priv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_44, secret);
        MLDSASigner signer = new MLDSASigner();
        signer.init(true, new ParametersWithContext(new ParametersWithRandom(priv, zeros()), CONTEXT));
        signer.update(message, 0, message.length);
        try {
            byte[] signature = signer.generateSignature();
            if(signature.length != SIGNATURE_SIZE) {
                throw new IllegalStateException("ML-DSA-44 signature length " + signature.length);
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
        MLDSAPublicKeyParameters pub = new MLDSAPublicKeyParameters(MLDSAParameters.ml_dsa_44, pubkey);
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
            throw new IllegalArgumentException("ML-DSA policy key count");
        }
        if(threshold < 1 || threshold > keyHashes.size()) {
            throw new IllegalArgumentException("ML-DSA policy threshold");
        }
        List<byte[]> ordered = new ArrayList<>(keyHashes);
        for(byte[] hash : ordered) {
            if(hash == null || hash.length != 32) {
                throw new IllegalArgumentException("ML-DSA policy hash must be 32 bytes");
            }
        }
        ordered.sort(KeyHashOrder.INSTANCE);
        for(int i = 1; i < ordered.size(); i++) {
            if(Arrays.equals(ordered.get(i - 1), ordered.get(i))) {
                throw new IllegalArgumentException("ML-DSA policy hashes must be strictly increasing");
            }
        }
        return com.sparrowwallet.drongo.protocol.ModernSig.policyScript(
                threshold, ordered, com.sparrowwallet.drongo.protocol.ModernSig.PARAM_DILITHIUM_44).getProgram();
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
