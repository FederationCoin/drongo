package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.Base58;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

public class ExtendedKeyTest {
    private static final String BITCOIN_XPUB =
            "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8";

    @AfterEach
    public void restoreNetwork() {
        Network.set(Network.MAINNET);
    }

    @Test
    public void recodedKeyRoundTripsOnMainnet() {
        Network.set(Network.MAINNET);
        String recoded = ChainEncoding.extendedKey(BITCOIN_XPUB);
        Assertions.assertNotEquals(BITCOIN_XPUB, recoded);
        Assertions.assertTrue(recoded.startsWith("xqiM"));
        ExtendedKey key = ExtendedKey.fromDescriptor(recoded);
        Assertions.assertEquals(recoded, key.toString());
        Assertions.assertEquals(ExtendedKey.Header.xpub, ExtendedKey.Header.fromExtendedKey(recoded));
    }

    @Test
    public void bitcoinXpubIsRejected() {
        Network.set(Network.MAINNET);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExtendedKey.fromDescriptor(BITCOIN_XPUB));
    }

    @Test
    public void bitcoinTpubIsRejected() {
        byte[] decoded = Base58.decodeChecked(BITCOIN_XPUB);
        ByteBuffer.wrap(decoded).putInt(0x043587CF);
        String bitcoinTpub = Base58.encodeChecked(decoded);
        Network.set(Network.TESTNET);
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExtendedKey.fromDescriptor(bitcoinTpub));
        Assertions.assertThrows(IllegalArgumentException.class, () -> ExtendedKey.Header.fromExtendedKey(bitcoinTpub));
    }
}
