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
        Assertions.assertTrue(ScriptType.P2A.isParkedOnThisChain());
    }

    @Test
    public void parkedAddressesCannotBeSent() {
        com.sparrowwallet.drongo.address.P2TRAddress taproot = new com.sparrowwallet.drongo.address.P2TRAddress(new byte[32]);
        com.sparrowwallet.drongo.address.InvalidAddressException parked = Assertions.assertThrows(
                com.sparrowwallet.drongo.address.InvalidAddressException.class, taproot::requireSendable);
        Assertions.assertEquals(ScriptType.TAPROOT_NOT_ENABLED_MESSAGE, parked.getMessage());

        com.sparrowwallet.drongo.address.P2AAddress anchor = new com.sparrowwallet.drongo.address.P2AAddress(new byte[2]);
        Assertions.assertThrows(com.sparrowwallet.drongo.address.InvalidAddressException.class, anchor::requireSendable);
    }

    @Test
    public void nativeSegwitCanBeSent() {
        com.sparrowwallet.drongo.address.P2WPKHAddress nativeSegwit = new com.sparrowwallet.drongo.address.P2WPKHAddress(new byte[20]);
        Assertions.assertDoesNotThrow(nativeSegwit::requireSendable);
    }
}
