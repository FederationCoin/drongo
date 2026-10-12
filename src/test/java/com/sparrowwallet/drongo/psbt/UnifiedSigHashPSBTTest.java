package com.sparrowwallet.drongo.psbt;

import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.crypto.ECKey;
import com.sparrowwallet.drongo.protocol.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Signing and verification through a PSBT that opts in, which is the path the wallet actually takes.
 *
 * UnifiedSigHashTest establishes that the digest agrees with the reference. What is left to show is
 * that a PSBT declaring an opted-in hash type reaches that digest rather than the legacy one, that the
 * hash type byte on the signature says so, and that the two messages are genuinely distinct, so a
 * signature made under one does not verify under the other.
 */
public class UnifiedSigHashPSBTTest {
    private static final ECKey KEY_ONE = ECKey.fromPrivate(Utils.hexToBytes("11".repeat(32)));
    private static final ECKey KEY_TWO = ECKey.fromPrivate(Utils.hexToBytes("22".repeat(32)));
    private static final long VALUE_ONE = 100000L;
    private static final long VALUE_TWO = 250000L;

    private PSBT twoInputPsbt(SigHash sigHash) {
        Script spkOne = ScriptType.P2WPKH.getOutputScript(PolicyType.SINGLE_MLDSA87, KEY_ONE);
        Script spkTwo = ScriptType.P2WPKH.getOutputScript(PolicyType.SINGLE_MLDSA87, KEY_TWO);

        Transaction transaction = new Transaction();
        transaction.setVersion(2);
        transaction.addInput(Sha256Hash.wrap(Utils.hexToBytes("aa".repeat(32))), 0, new Script(new byte[0]));
        transaction.addInput(Sha256Hash.wrap(Utils.hexToBytes("bb".repeat(32))), 1, new Script(new byte[0]));
        transaction.addOutput(VALUE_ONE + VALUE_TWO - 1000L, spkOne);

        PSBT psbt = new PSBT(transaction);
        List<PSBTInput> inputs = psbt.getPsbtInputs();
        inputs.get(0).setWitnessUtxo(new TransactionOutput(null, VALUE_ONE, spkOne.getProgram()));
        inputs.get(1).setWitnessUtxo(new TransactionOutput(null, VALUE_TWO, spkTwo.getProgram()));
        for(PSBTInput input : inputs) {
            input.setSigHash(sigHash);
        }

        return psbt;
    }

    private ECKey outputKey(ECKey privKey) {
        return ScriptType.P2WPKH.getOutputKey(PolicyType.SINGLE_MLDSA87, privKey);
    }

    @Test
    public void testSigningAnOptedInPsbtCarriesTheHashTypeByte() {
        PSBT psbt = twoInputPsbt(SigHash.UNIFIED_ALL);
        List<PSBTInput> inputs = psbt.getPsbtInputs();
        inputs.get(0).sign(outputKey(KEY_ONE));
        inputs.get(1).sign(outputKey(KEY_TWO));

        for(PSBTInput input : inputs) {
            Assertions.assertFalse(input.getPartialSignatures().isEmpty(), "Input produced no signature");
            for(TransactionSignature signature : input.getPartialSignatures().values()) {
                Assertions.assertEquals(SigHash.UNIFIED_ALL.byteValue(), signature.sighashFlags,
                        "Signature does not carry the opt-in hash type");
            }
        }
    }

    /**
     * The signature must verify against the unified digest, which is what proves signing and verification
     * agree on which message was signed rather than only that a signature was produced.
     */
    @Test
    public void testAnOptedInSignatureVerifies() {
        PSBT psbt = twoInputPsbt(SigHash.UNIFIED_ALL);
        List<PSBTInput> inputs = psbt.getPsbtInputs();
        inputs.get(0).sign(outputKey(KEY_ONE));
        inputs.get(1).sign(outputKey(KEY_TWO));

        Map<ECKey, TransactionSignature> first = inputs.get(0).getSigningKeys(Set.of(ECKey.fromPublicOnly(outputKey(KEY_ONE))));
        Assertions.assertEquals(1, first.size(), "The opted-in signature did not verify");
        Map<ECKey, TransactionSignature> second = inputs.get(1).getSigningKeys(Set.of(ECKey.fromPublicOnly(outputKey(KEY_TWO))));
        Assertions.assertEquals(1, second.size(), "The opted-in signature did not verify");
    }

    /**
     * The PSBT path must reach the same digest as calling the algorithm directly, with the segwit v0
     * script type byte and an empty scriptCode. Anything else would verify here and be rejected by a node.
     */
    @Test
    public void testPsbtDigestMatchesTheAlgorithmDirectly() {
        PSBT psbt = twoInputPsbt(SigHash.UNIFIED_ALL);
        List<PSBTInput> inputs = psbt.getPsbtInputs();
        inputs.get(0).sign(outputKey(KEY_ONE));

        List<TransactionOutput> spentUtxos = inputs.stream().map(PSBTInput::getUtxo).toList();
        Sha256Hash expected = psbt.getTransaction().hashForUnifiedSignature(spentUtxos, 0, UnifiedScriptType.WITNESS_V0,
                new byte[0], SigHash.UNIFIED_ALL.byteValue(), null, null, null);

        ECKey pubKey = ECKey.fromPublicOnly(outputKey(KEY_ONE));
        TransactionSignature signature = inputs.get(0).getPartialSignatures().get(pubKey);
        Assertions.assertNotNull(signature, "No signature for the signing key");
        Assertions.assertTrue(pubKey.verify(expected, signature), "The PSBT signed a different message than the algorithm produces");
    }

    /**
     * Opting in must change which message is signed. If a legacy signature verified against the unified
     * digest the opt-in would be doing nothing, and the replay protection it exists for would be absent.
     */
    @Test
    public void testALegacySignatureDoesNotVerifyAsUnified() {
        PSBT legacyPsbt = twoInputPsbt(SigHash.ALL);
        legacyPsbt.getPsbtInputs().get(0).sign(outputKey(KEY_ONE));
        TransactionSignature legacySignature = legacyPsbt.getPsbtInputs().get(0).getPartialSignatures().get(ECKey.fromPublicOnly(outputKey(KEY_ONE)));
        Assertions.assertNotNull(legacySignature);
        Assertions.assertEquals(SigHash.ALL.byteValue(), legacySignature.sighashFlags);

        PSBT unifiedPsbt = twoInputPsbt(SigHash.UNIFIED_ALL);
        List<PSBTInput> inputs = unifiedPsbt.getPsbtInputs();
        List<TransactionOutput> spentUtxos = inputs.stream().map(PSBTInput::getUtxo).toList();
        Sha256Hash unifiedDigest = unifiedPsbt.getTransaction().hashForUnifiedSignature(spentUtxos, 0, UnifiedScriptType.WITNESS_V0,
                new byte[0], SigHash.UNIFIED_ALL.byteValue(), null, null, null);

        Assertions.assertFalse(ECKey.fromPublicOnly(outputKey(KEY_ONE)).verify(unifiedDigest, legacySignature),
                "A legacy signature verified against the unified message");
    }

    /**
     * A PSBT carrying an opted-in hash type must survive serialisation, or a signing device is handed a
     * request it cannot read.
     */
    @Test
    public void testAnOptedInHashTypeRoundTripsThroughSerialisation() throws PSBTParseException {
        PSBT psbt = twoInputPsbt(SigHash.UNIFIED_ALL);
        PSBT parsed = new PSBT(psbt.serialize());
        for(PSBTInput input : parsed.getPsbtInputs()) {
            Assertions.assertEquals(SigHash.UNIFIED_ALL, input.getSigHash());
        }
    }

    /**
     * Product secp v0 signing with no PSBT sighash field must emit UNIFIED_ALL over empty scriptCode,
     * the same digest the node hashes. BIP143 implied-P2PKH is a different message and must not be
     * what the wallet signs.
     */
    @Test
    public void testProductSignWithoutSetSigHashUsesUnifiedAllAndEmptyScriptCode() {
        Script spk = ScriptType.P2WPKH.getOutputScript(KEY_ONE.getPubKeyHash());
        Transaction transaction = new Transaction();
        transaction.setVersion(2);
        transaction.addInput(Sha256Hash.wrap(Utils.hexToBytes("aa".repeat(32))), 0, new Script(new byte[0]));
        transaction.addOutput(VALUE_ONE - 1000L, spk);

        PSBT psbt = new PSBT(transaction);
        PSBTInput input = psbt.getPsbtInputs().get(0);
        input.setWitnessUtxo(new TransactionOutput(null, VALUE_ONE, spk.getProgram()));
        Assertions.assertNull(input.getSigHash(), "This case is the missing PSBT sighash field");

        Assertions.assertTrue(input.sign(KEY_ONE), "Product sign produced no signature");
        TransactionSignature signature = input.getPartialSignatures().get(ECKey.fromPublicOnly(KEY_ONE));
        Assertions.assertNotNull(signature, "No signature for the signing key");
        Assertions.assertEquals(SigHash.UNIFIED_ALL.byteValue(), signature.sighashFlags,
                "Product sign without setSigHash must emit UNIFIED_ALL");

        List<TransactionOutput> spent = List.of(input.getUtxo());
        Sha256Hash emptyScriptCode = psbt.getTransaction().hashForUnifiedSignature(spent, 0, UnifiedScriptType.WITNESS_V0,
                new byte[0], SigHash.UNIFIED_ALL.byteValue(), null, null, null);
        Assertions.assertTrue(ECKey.fromPublicOnly(KEY_ONE).verify(emptyScriptCode, signature),
                "Product sign hashed something other than empty scriptCode UNIFIED_ALL");

        Script p2pkhWrap = ScriptType.P2PKH.getOutputScript(KEY_ONE.getPubKeyHash());
        Sha256Hash wrappedUnified = psbt.getTransaction().hashForUnifiedSignature(spent, 0, UnifiedScriptType.WITNESS_V0,
                p2pkhWrap.getProgram(), SigHash.UNIFIED_ALL.byteValue(), null, null, null);
        Assertions.assertNotEquals(emptyScriptCode, wrappedUnified,
                "Empty scriptCode and the BIP143 P2PKH wrap must be distinct messages");
        Sha256Hash bip143 = psbt.getTransaction().hashForWitnessSignature(0, p2pkhWrap, VALUE_ONE, SigHash.ALL);
        Assertions.assertFalse(ECKey.fromPublicOnly(KEY_ONE).verify(bip143, signature),
                "Product sign produced a BIP143 signature");
    }
}
