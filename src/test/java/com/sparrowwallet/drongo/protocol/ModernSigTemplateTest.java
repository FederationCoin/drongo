package com.sparrowwallet.drongo.protocol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ModernSigTemplateTest {
    @Test
    public void unknownKindIsNotATemplate() {
        byte[] fingerprint = new byte[32];
        fingerprint[0] = 0x11;
        List<ScriptChunk> chunks = new java.util.ArrayList<>();
        chunks.add(new ScriptChunk(fingerprint.length, fingerprint));
        chunks.add(new ScriptChunk(Script.encodeToOpN(2), null));
        chunks.add(new ScriptChunk(Script.encodeToOpN(ModernSig.PARAM_DILITHIUM_44), null));
        chunks.add(new ScriptChunk(ScriptOpCodes.OP_CHECKMODERNSIG, null));
        Script unknown = new Script(chunks);
        Assertions.assertNotNull(ModernSig.matchSingle(unknown));
        Assertions.assertFalse(ModernSig.isKnownKind(2, ModernSig.PARAM_DILITHIUM_44));
        Assertions.assertFalse(ModernSig.isTemplate(unknown));
    }

    @Test
    public void knownKindsAreTemplates() {
        byte[] fingerprint = new byte[32];
        fingerprint[0] = 0x22;
        Assertions.assertTrue(ModernSig.isTemplate(ModernSig.singleScript(fingerprint, ModernSig.PARAM_DILITHIUM_44)));
        Assertions.assertTrue(ModernSig.isTemplate(ModernSig.singleScript(fingerprint, ModernSig.PARAM_DILITHIUM_87)));
        byte[] h1 = new byte[32];
        h1[0] = 0x01;
        byte[] h2 = new byte[32];
        h2[0] = 0x02;
        Assertions.assertTrue(ModernSig.isTemplate(ModernSig.policyScript(1, List.of(h1, h2), ModernSig.PARAM_DILITHIUM_87)));
    }
}
