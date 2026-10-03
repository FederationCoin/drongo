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
    public void newWalletsOfferMlDsaOnly() {
        Assertions.assertEquals(List.of(ScriptType.MLDSA_SINGLE), ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_HD));
        Assertions.assertEquals(List.of(ScriptType.MLDSA_MULTI), ScriptType.getAddressableScriptTypes(PolicyType.MULTI_HD));
        Assertions.assertTrue(ScriptType.getAddressableScriptTypes(PolicyType.SINGLE_SP).isEmpty());
    }

    @Test
    public void existingTypesRemainValidForPolicy() {
        Assertions.assertTrue(ScriptType.P2WPKH.isAllowed(PolicyType.SINGLE_HD));
        Assertions.assertTrue(ScriptType.P2PKH.isAllowed(PolicyType.SINGLE_HD));
        Assertions.assertFalse(ScriptType.P2WPKH.isOfferedForNewWallets());
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
    public void heritageSegwitCannotBeSent() {
        com.sparrowwallet.drongo.address.P2WPKHAddress nativeSegwit = new com.sparrowwallet.drongo.address.P2WPKHAddress(new byte[20]);
        Assertions.assertThrows(com.sparrowwallet.drongo.address.InvalidAddressException.class, nativeSegwit::requireSendable);
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
