package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.MlDsaAddress;
import com.sparrowwallet.drongo.policy.PolicyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScriptTypeOfferTest {
    @Test
    public void newWalletsOfferDilithiumAndWarnedSecp() {
        List<ScriptType> single = ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_HD);
        Assertions.assertTrue(single.contains(ScriptType.MLDSA87_SINGLE));
        Assertions.assertTrue(single.contains(ScriptType.MLDSA_SINGLE));
        Assertions.assertTrue(single.contains(ScriptType.P2WPKH));
        List<ScriptType> multi = ScriptType.getAddressableScriptTypes(PolicyType.MULTI_HD);
        Assertions.assertTrue(multi.contains(ScriptType.MLDSA87_MULTI));
        Assertions.assertTrue(multi.contains(ScriptType.MLDSA_MULTI));
        Assertions.assertTrue(ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_SP).isEmpty());
        Assertions.assertEquals(ScriptType.MLDSA87_SINGLE, PolicyType.SINGLE_HD.getDefaultScriptType());
        Assertions.assertTrue(ScriptType.P2WPKH.needsQuantumWarning());
        Assertions.assertEquals(SecpCheapOut.WARNING, SecpCheapOut.warnReceive());
        Assertions.assertEquals(SecpCheapOut.WARNING, SecpCheapOut.warnSend());
    }

    @Test
    public void existingTypesRemainValidForPolicy() {
        Assertions.assertTrue(ScriptType.P2WPKH.isAllowed(PolicyType.SINGLE_HD));
        Assertions.assertTrue(ScriptType.P2PKH.isAllowed(PolicyType.SINGLE_HD));
        Assertions.assertTrue(ScriptType.P2WPKH.isOfferedForNewWallets());
        Assertions.assertFalse(ScriptType.getScriptTypesForPolicyType(PolicyType.SINGLE_HD).contains(ScriptType.P2TR));
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
    public void warnedSecpCanBeSent() {
        com.sparrowwallet.drongo.address.P2WPKHAddress nativeSegwit = new com.sparrowwallet.drongo.address.P2WPKHAddress(new byte[20]);
        Assertions.assertDoesNotThrow(nativeSegwit::requireSendable);
        Assertions.assertTrue(nativeSegwit.getScriptType().needsQuantumWarning());
    }

    @Test
    public void witnessV0ProgramIsMlDsaAddress() throws Exception {
        byte[] program = new byte[32];
        program[0] = 0x2a;
        MlDsaAddress created = new MlDsaAddress(program);
        Address parsed = Address.fromString(Network.TESTNET, created.getAddress(Network.TESTNET));
        Assertions.assertInstanceOf(MlDsaAddress.class, parsed);
        Assertions.assertArrayEquals(program, parsed.getData());
        Assertions.assertDoesNotThrow(parsed::requireSendable);
    }
}
