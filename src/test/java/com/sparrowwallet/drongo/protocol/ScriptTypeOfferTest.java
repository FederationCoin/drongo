package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.policy.PolicyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScriptTypeOfferTest {
    @Test
    public void newWalletsOfferNativeSegwitOnly() {
        Assertions.assertEquals(List.of(ScriptType.P2WPKH), ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_HD));
        Assertions.assertEquals(List.of(ScriptType.P2WSH), ScriptType.getAddressableScriptTypes(PolicyType.MULTI_HD));
        Assertions.assertTrue(ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_SP).isEmpty());
    }

    @Test
    public void existingTypesRemainValidForPolicy() {
        Assertions.assertTrue(ScriptType.getScriptTypesForPolicyType(PolicyType.SINGLE_HD).contains(ScriptType.P2TR));
        Assertions.assertTrue(ScriptType.getScriptTypesForPolicyType(PolicyType.SINGLE_HD).contains(ScriptType.P2PKH));
        Assertions.assertTrue(ScriptType.P2TR.isParkedOnThisChain());
        Assertions.assertFalse(ScriptType.P2WPKH.isParkedOnThisChain());
    }
}
