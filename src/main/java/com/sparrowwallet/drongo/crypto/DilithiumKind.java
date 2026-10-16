package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.protocol.MlDsa44Multisig;
import com.sparrowwallet.drongo.protocol.MlDsa87Multisig;
import com.sparrowwallet.drongo.protocol.MlDsa87Spend;
import com.sparrowwallet.drongo.protocol.MlDsaSpend;
import com.sparrowwallet.drongo.protocol.ModernSig;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionWitness;

import java.util.ArrayList;
import java.util.List;

/**
 * Closed Dilithium spend kinds. Each constant owns sizes, tagged-KDF kind, KEY hash, and dummy witness.
 * Wallet and ScriptType ask; they do not switch on 44 vs 87.
 */
public enum DilithiumKind {
    SINGLE_44(MlDsaSeed.Kind.DILITHIUM_44, ModernSig.PARAM_DILITHIUM_44, false) {
        @Override
        public int pubkeySize() {
            return MlDsa44.PUBLIC_KEY_SIZE;
        }

        @Override
        public int signatureSize() {
            return MlDsa44.SIGNATURE_SIZE;
        }

        @Override
        public Keypair keygen(byte[] seed) {
            MlDsa44.Keypair pair = MlDsa44.keygen(seed);
            return new Keypair(pair.pubkey(), pair.secret());
        }

        @Override
        public byte[] keyHash(byte[] pubkey) {
            return MlDsa44.keyHash(pubkey);
        }

        @Override
        public byte[] policyProgram(int threshold, List<byte[]> hashes) {
            throw new UnsupportedOperationException("single-key Dilithium 44 has no policy program");
        }

        @Override
        public TransactionWitness dummyWitness(Transaction transaction, int keyCount, int threshold) {
            return MlDsaSpend.sendSingleKey(transaction, new byte[pubkeySize()], new byte[signatureSize()]);
        }

        @Override
        public TransactionWitness finalizeSingle(Transaction transaction, byte[] pubkey, byte[] signature) {
            return MlDsaSpend.sendSingleKey(transaction, pubkey, signature);
        }

        @Override
        public TransactionWitness finalizePolicy(Transaction transaction, List<byte[]> slots) {
            throw new UnsupportedOperationException("single-key Dilithium 44 has no policy witness");
        }

        @Override
        public byte[] sign(byte[] secret, byte[] message) {
            return MlDsa44.sign(secret, message);
        }
    },
    SINGLE_87(MlDsaSeed.Kind.DILITHIUM_87, ModernSig.PARAM_DILITHIUM_87, false) {
        @Override
        public int pubkeySize() {
            return MlDsa87.PUBLIC_KEY_SIZE;
        }

        @Override
        public int signatureSize() {
            return MlDsa87.SIGNATURE_SIZE;
        }

        @Override
        public Keypair keygen(byte[] seed) {
            MlDsa87.Keypair pair = MlDsa87.keygen(seed);
            return new Keypair(pair.pubkey(), pair.secret());
        }

        @Override
        public byte[] keyHash(byte[] pubkey) {
            return MlDsa87.keyHash(pubkey);
        }

        @Override
        public byte[] policyProgram(int threshold, List<byte[]> hashes) {
            throw new UnsupportedOperationException("single-key Dilithium 87 has no policy program");
        }

        @Override
        public TransactionWitness dummyWitness(Transaction transaction, int keyCount, int threshold) {
            return MlDsa87Spend.send(transaction, new byte[pubkeySize()], new byte[signatureSize()]);
        }

        @Override
        public TransactionWitness finalizeSingle(Transaction transaction, byte[] pubkey, byte[] signature) {
            return MlDsa87Spend.send(transaction, pubkey, signature);
        }

        @Override
        public TransactionWitness finalizePolicy(Transaction transaction, List<byte[]> slots) {
            throw new UnsupportedOperationException("single-key Dilithium 87 has no policy witness");
        }

        @Override
        public byte[] sign(byte[] secret, byte[] message) {
            return MlDsa87.sign(secret, message);
        }
    },
    POLICY_44(MlDsaSeed.Kind.POLICY_44, ModernSig.PARAM_DILITHIUM_44, true) {
        @Override
        public int pubkeySize() {
            return MlDsa44.PUBLIC_KEY_SIZE;
        }

        @Override
        public int signatureSize() {
            return MlDsa44.SIGNATURE_SIZE;
        }

        @Override
        public Keypair keygen(byte[] seed) {
            MlDsa44.Keypair pair = MlDsa44.keygen(seed);
            return new Keypair(pair.pubkey(), pair.secret());
        }

        @Override
        public byte[] keyHash(byte[] pubkey) {
            return MlDsa44.keyHash(pubkey);
        }

        @Override
        public byte[] policyProgram(int threshold, List<byte[]> hashes) {
            return MlDsa44.policyProgram(threshold, hashes);
        }

        @Override
        public TransactionWitness dummyWitness(Transaction transaction, int keyCount, int threshold) {
            List<byte[]> slots = new ArrayList<>();
            int item = pubkeySize() + signatureSize();
            for(int i = 0; i < Math.max(1, keyCount); i++) {
                slots.add(i < threshold ? new byte[item] : new byte[0]);
            }
            return MlDsa44Multisig.send(transaction, slots);
        }

        @Override
        public TransactionWitness finalizeSingle(Transaction transaction, byte[] pubkey, byte[] signature) {
            throw new UnsupportedOperationException("policy Dilithium 44 has no single-key witness");
        }

        @Override
        public TransactionWitness finalizePolicy(Transaction transaction, List<byte[]> slots) {
            return MlDsaSpend.sendMultisig(transaction, slots);
        }

        @Override
        public byte[] sign(byte[] secret, byte[] message) {
            return MlDsa44.sign(secret, message);
        }
    },
    POLICY_87(MlDsaSeed.Kind.POLICY_87, ModernSig.PARAM_DILITHIUM_87, true) {
        @Override
        public int pubkeySize() {
            return MlDsa87.PUBLIC_KEY_SIZE;
        }

        @Override
        public int signatureSize() {
            return MlDsa87.SIGNATURE_SIZE;
        }

        @Override
        public Keypair keygen(byte[] seed) {
            MlDsa87.Keypair pair = MlDsa87.keygen(seed);
            return new Keypair(pair.pubkey(), pair.secret());
        }

        @Override
        public byte[] keyHash(byte[] pubkey) {
            return MlDsa87.keyHash(pubkey);
        }

        @Override
        public byte[] policyProgram(int threshold, List<byte[]> hashes) {
            return MlDsa87.policyProgram(threshold, hashes);
        }

        @Override
        public TransactionWitness dummyWitness(Transaction transaction, int keyCount, int threshold) {
            List<byte[]> slots = new ArrayList<>();
            int item = pubkeySize() + signatureSize();
            for(int i = 0; i < Math.max(1, keyCount); i++) {
                slots.add(i < threshold ? new byte[item] : new byte[0]);
            }
            return MlDsa87Multisig.send(transaction, slots);
        }

        @Override
        public TransactionWitness finalizeSingle(Transaction transaction, byte[] pubkey, byte[] signature) {
            throw new UnsupportedOperationException("policy Dilithium 87 has no single-key witness");
        }

        @Override
        public TransactionWitness finalizePolicy(Transaction transaction, List<byte[]> slots) {
            return MlDsa87Multisig.send(transaction, slots);
        }

        @Override
        public byte[] sign(byte[] secret, byte[] message) {
            return MlDsa87.sign(secret, message);
        }
    };

    public record Keypair(byte[] pubkey, byte[] secret) {
    }

    private final MlDsaSeed.Kind seedKind;
    private final int param;
    private final boolean policy;

    DilithiumKind(MlDsaSeed.Kind seedKind, int param, boolean policy) {
        this.seedKind = seedKind;
        this.param = param;
        this.policy = policy;
    }

    public MlDsaSeed.Kind seedKind() {
        return seedKind;
    }

    public int param() {
        return param;
    }

    public boolean isPolicy() {
        return policy;
    }

    public abstract int pubkeySize();

    public abstract int signatureSize();

    public abstract Keypair keygen(byte[] seed);

    public abstract byte[] keyHash(byte[] pubkey);

    public abstract byte[] policyProgram(int threshold, List<byte[]> hashes);

    public abstract TransactionWitness dummyWitness(Transaction transaction, int keyCount, int threshold);

    public abstract TransactionWitness finalizeSingle(Transaction transaction, byte[] pubkey, byte[] signature);

    public abstract TransactionWitness finalizePolicy(Transaction transaction, List<byte[]> slots);

    public abstract byte[] sign(byte[] secret, byte[] message);
}
