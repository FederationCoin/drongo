package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.crypto.ECKey;

import java.util.List;

/**
 * secp cheap-out: witness v0 / 20-byte HASH160, P2WPKH stack [sig, pubkey].
 * Digest is the unified Blake2b sighash. Warning lives on SecpCheapOut.
 */
public final class SecpSpend {
    private SecpSpend() {
    }

    public static TransactionWitness send(Transaction transaction, byte[] signatureWithHashtype, byte[] compressedPubkey) {
        if(compressedPubkey.length != 33) {
            throw new ProtocolException("secp pubkey must be compressed");
        }
        if(signatureWithHashtype.length < 9 || signatureWithHashtype.length > 73) {
            throw new ProtocolException("secp signature length");
        }
        if((signatureWithHashtype[signatureWithHashtype.length - 1] & 0xff) != (SigHash.UNIFIED_ALL.value & 0xff)) {
            throw new ProtocolException("secp must use SIGHASH_ALL|SIGHASH_UNIFIED");
        }
        return new TransactionWitness(transaction, List.of(signatureWithHashtype, compressedPubkey));
    }

    public static boolean programMatches(byte[] program, byte[] compressedPubkey) {
        return program != null && program.length == 20 && java.util.Arrays.equals(program, ECKey.fromPublicOnly(compressedPubkey).getPubKeyHash());
    }
}
