package com.sparrowwallet.drongo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkIdentityTest {
    @Test
    public void defaultPortsMatchNodeRpc() {
        Assertions.assertEquals(4094, Network.MAINNET.getDefaultPort());
        Assertions.assertEquals(35332, Network.TESTNET.getDefaultPort());
        Assertions.assertEquals(25443, Network.REGTEST.getDefaultPort());
        Assertions.assertEquals(26332, Network.SIGNET.getDefaultPort());
        Assertions.assertEquals(45332, Network.TESTNET4.getDefaultPort());
    }

    @Test
    public void bech32Hrps() {
        Assertions.assertEquals("gfcn", Network.MAINNET.getBech32AddressHRP());
        Assertions.assertEquals("tgfcn", Network.TESTNET.getBech32AddressHRP());
        Assertions.assertEquals("gfcnrt", Network.REGTEST.getBech32AddressHRP());
        Assertions.assertEquals("tgfcn", Network.SIGNET.getBech32AddressHRP());
        Assertions.assertEquals("tgfcn", Network.TESTNET4.getBech32AddressHRP());
    }

    @Test
    public void blake2bHeightIsZeroExceptRegtest() {
        Assertions.assertEquals(Integer.valueOf(0), Network.MAINNET.getBlake2bHeight());
        Assertions.assertEquals(Integer.valueOf(0), Network.TESTNET.getBlake2bHeight());
        Assertions.assertEquals(Integer.valueOf(0), Network.TESTNET4.getBlake2bHeight());
        Assertions.assertEquals(Integer.valueOf(0), Network.SIGNET.getBlake2bHeight());
        Assertions.assertNull(Network.REGTEST.getBlake2bHeight());
    }

    @Test
    public void blake2bTargetShiftIsZero() {
        for(Network network : Network.values()) {
            Assertions.assertEquals(0, network.getBlake2bTargetShift());
        }
    }

    @Test
    public void proofOfWorkLimitForks() {
        Assertions.assertEquals(Utils.decodeCompactBits(0x207fffffL), Network.REGTEST.getProofOfWorkLimit());
        Assertions.assertEquals(Utils.decodeCompactBits(0x1e0377aeL), Network.SIGNET.getProofOfWorkLimit());
        Assertions.assertEquals(Utils.decodeCompactBits(0x1e00ffffL), Network.MAINNET.getProofOfWorkLimit());
        Assertions.assertEquals(Utils.decodeCompactBits(0x1e00ffffL), Network.TESTNET.getProofOfWorkLimit());
        Assertions.assertEquals(Utils.decodeCompactBits(0x1e00ffffL), Network.TESTNET4.getProofOfWorkLimit());
    }

    @Test
    public void dummyMainDisplayName() {
        Assertions.assertEquals("Mainnet (not live)", Network.MAINNET.toDisplayString());
        Assertions.assertEquals("Testnet3", Network.TESTNET.toDisplayString());
    }
}
