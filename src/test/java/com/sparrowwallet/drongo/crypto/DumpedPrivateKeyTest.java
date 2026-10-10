package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.ChainEncoding;
import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DumpedPrivateKeyTest {
    private static final String COMPRESSED_WIF_BTC = "L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP";
    private static final String UNCOMPRESSED_WIF_BTC = "5KN7MzqK5wt2TP1fQCYyHBtDrXdJuXbUzm4A9rKAteGu3Qi5CVR";

    @Test
    public void testBitcoinWifRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> DumpedPrivateKey.fromBase58(COMPRESSED_WIF_BTC));
        Assertions.assertThrows(IllegalArgumentException.class, () -> DumpedPrivateKey.fromBase58(UNCOMPRESSED_WIF_BTC));
    }

    @Test
    public void testCompressedRoundTrip() {
        ECKey key = ChainEncoding.keyFromPublishedWif(COMPRESSED_WIF_BTC);
        DumpedPrivateKey encoded = key.getPrivateKeyEncoded();
        String wif = encoded.toBase58();
        DumpedPrivateKey parsed = DumpedPrivateKey.fromBase58(wif);
        Assertions.assertTrue(parsed.getKey().isCompressed());
        Assertions.assertEquals(wif, parsed.toBase58());
        Assertions.assertEquals(wif, parsed.getKey().getPrivateKeyEncoded().toBase58());
        Assertions.assertNotEquals(COMPRESSED_WIF_BTC, wif);
    }

    @Test
    public void testUncompressedRoundTrip() {
        ECKey key = ChainEncoding.keyFromPublishedWif(UNCOMPRESSED_WIF_BTC);
        DumpedPrivateKey encoded = key.getPrivateKeyEncoded();
        String wif = encoded.toBase58();
        DumpedPrivateKey parsed = DumpedPrivateKey.fromBase58(wif);
        Assertions.assertFalse(parsed.getKey().isCompressed());
        Assertions.assertEquals(wif, parsed.toBase58());
        Assertions.assertEquals(wif, parsed.getKey().getPrivateKeyEncoded().toBase58());
        Assertions.assertNotEquals(UNCOMPRESSED_WIF_BTC, wif);
    }

    @Test
    public void testKeyMatchesRegardlessOfConstruction() {
        for(String bitcoinWif : new String[] {COMPRESSED_WIF_BTC, UNCOMPRESSED_WIF_BTC}) {
            ECKey key = ChainEncoding.keyFromPublishedWif(bitcoinWif);
            DumpedPrivateKey encoded = key.getPrivateKeyEncoded();
            DumpedPrivateKey parsed = DumpedPrivateKey.fromBase58(encoded.toBase58());
            Assertions.assertEquals(Utils.bytesToHex(parsed.getKey().getPubKey()), Utils.bytesToHex(encoded.getKey().getPubKey()));
            Assertions.assertEquals(parsed, encoded);
            Assertions.assertEquals(parsed.hashCode(), encoded.hashCode());
        }
    }

    @Test
    public void testCompressedAndUncompressedDifferDespiteSharedScalar() {
        //These two BIP38 vectors are the same 32 byte scalar, so only the compression marker separates them
        ECKey compressed = ChainEncoding.keyFromPublishedWif(COMPRESSED_WIF_BTC);
        ECKey uncompressed = ChainEncoding.keyFromPublishedWif(UNCOMPRESSED_WIF_BTC);
        Assertions.assertEquals(compressed.getPrivKey(), uncompressed.getPrivKey());
        Assertions.assertNotEquals(Utils.bytesToHex(compressed.getPubKey()), Utils.bytesToHex(uncompressed.getPubKey()));
    }
}
