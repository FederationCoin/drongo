package com.sparrowwallet.drongo.policy;

import com.sparrowwallet.drongo.protocol.ScriptType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolicyTypeTest {
    @Test
    public void forScriptPairsOfferedKinds() {
        Assertions.assertEquals(PolicyType.SINGLE_MLDSA87, PolicyType.forScript(ScriptType.MLDSA87_SINGLE, false));
        Assertions.assertEquals(PolicyType.MULTI_MLDSA87, PolicyType.forScript(ScriptType.MLDSA87_MULTI, true));
        Assertions.assertEquals(PolicyType.SINGLE_MLDSA44, PolicyType.forScript(ScriptType.MLDSA_SINGLE, false));
        Assertions.assertEquals(PolicyType.MULTI_MLDSA44, PolicyType.forScript(ScriptType.MLDSA_MULTI, true));
        Assertions.assertEquals(PolicyType.SINGLE_SEGWIT, PolicyType.forScript(ScriptType.P2WPKH, false));
        Assertions.assertEquals(PolicyType.MULTI_SEGWIT, PolicyType.forScript(ScriptType.P2WSH, true));
        Assertions.assertEquals(PolicyType.SINGLE_SEGWIT, PolicyType.forScript(ScriptType.P2PKH, false));
        Assertions.assertEquals(PolicyType.MULTI_SEGWIT, PolicyType.forScript(ScriptType.P2SH, true));
    }

    @Test
    public void offeredAndSegwitVisit() {
        Assertions.assertTrue(PolicyType.SINGLE_MLDSA87.isOfferedForNewWallets());
        Assertions.assertFalse(PolicyType.MULTI_MLDSA87.isOfferedForNewWallets());
        Assertions.assertTrue(PolicyType.SINGLE_MLDSA44.isOfferedForNewWallets());
        Assertions.assertFalse(PolicyType.MULTI_MLDSA44.isOfferedForNewWallets());
        Assertions.assertTrue(PolicyType.SINGLE_SEGWIT.isOfferedForNewWallets());
        Assertions.assertFalse(PolicyType.MULTI_SEGWIT.isOfferedForNewWallets());
        Assertions.assertFalse(PolicyType.SINGLE_SP.isOfferedForNewWallets());
        Assertions.assertTrue(PolicyType.SINGLE_SEGWIT.isSegwit());
        Assertions.assertTrue(PolicyType.MULTI_SEGWIT.isSegwit());
        Assertions.assertFalse(PolicyType.SINGLE_MLDSA87.isSegwit());
        Assertions.assertFalse(PolicyType.SINGLE_SP.isSegwit());
    }

    @Test
    public void forScriptRejectsEmptyAllowList() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> PolicyType.forScript(ScriptType.P2A, false));
    }
}
