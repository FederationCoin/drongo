package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.MlDsa44Address;
import com.sparrowwallet.drongo.address.MlDsa87Address;
import com.sparrowwallet.drongo.policy.PolicyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScriptTypeOfferTest {
    @Test
    public void newWalletsOfferEachNamedPolicy() {
        List<ScriptType> d87 = ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_MLDSA87);
        Assertions.assertEquals(List.of(ScriptType.MLDSA87_SINGLE), d87);
        Assertions.assertEquals(List.of(ScriptType.MLDSA_SINGLE), ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_MLDSA44));
        Assertions.assertEquals(List.of(ScriptType.P2WPKH), ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_SEGWIT));
        Assertions.assertEquals(List.of(ScriptType.MLDSA87_MULTI), ScriptType.getAddressableScriptTypes(PolicyType.MULTI_MLDSA87));
        Assertions.assertEquals(List.of(ScriptType.MLDSA_MULTI), ScriptType.getAddressableScriptTypes(PolicyType.MULTI_MLDSA44));
        Assertions.assertEquals(List.of(ScriptType.P2WSH), ScriptType.getAddressableScriptTypes(PolicyType.MULTI_SEGWIT));
        Assertions.assertTrue(ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_SP).isEmpty());
        Assertions.assertEquals(ScriptType.MLDSA87_SINGLE, PolicyType.SINGLE_MLDSA87.getDefaultScriptType());
        Assertions.assertTrue(PolicyType.SINGLE_MLDSA87.isOfferedForNewWallets());
        Assertions.assertFalse(PolicyType.SINGLE_SP.isOfferedForNewWallets());
    }

    @Test
    public void existingTypesRemainValidForPolicy() {
        Assertions.assertTrue(ScriptType.P2WPKH.isAllowed(PolicyType.SINGLE_SEGWIT));
        Assertions.assertFalse(ScriptType.P2PKH.isAllowed(PolicyType.SINGLE_MLDSA87));
        Assertions.assertTrue(ScriptType.P2WPKH.isOfferedForNewWallets());
        Assertions.assertTrue(ScriptType.P2WSH.isOfferedForNewWallets());
        Assertions.assertFalse(ScriptType.getScriptTypesForPolicyType(PolicyType.SINGLE_MLDSA87).contains(ScriptType.P2TR));
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
    public void segwitCanBeSent() {
        com.sparrowwallet.drongo.address.P2WPKHAddress nativeSegwit = new com.sparrowwallet.drongo.address.P2WPKHAddress(new byte[20]);
        Assertions.assertDoesNotThrow(nativeSegwit::requireSendable);
    }

    @Test
    public void witnessV0ProgramIsExplicitMlDsaType() throws Exception {
        byte[] program = new byte[32];
        program[0] = 0x2a;
        MlDsa44Address created44 = new MlDsa44Address(program);
        Assertions.assertEquals(ScriptType.MLDSA_SINGLE, created44.getScriptType());
        MlDsa87Address created87 = new MlDsa87Address(program);
        Assertions.assertEquals(ScriptType.MLDSA87_SINGLE, created87.getScriptType());
        Address parsed = Address.fromString(Network.TESTNET, created87.getAddress(Network.TESTNET));
        Assertions.assertInstanceOf(MlDsa87Address.class, parsed);
        Assertions.assertArrayEquals(program, parsed.getData());
        Assertions.assertDoesNotThrow(parsed::requireSendable);
    }
}
