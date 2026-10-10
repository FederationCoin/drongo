package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.ChainEncoding;
import com.sparrowwallet.drongo.protocol.Base58;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class BIP38Test {
    //Published BIP38 vectors (https://github.com/bitcoin/bips/blob/master/bip-0038.mediawiki). The address hash
    //inside each ciphertext is Bitcoin P2PKH, so the correct passphrase fails the MAC on this chain.
    private static final String NO_EC_UNCOMPRESSED = "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg";
    private static final String NO_EC_COMPRESSED = "6PYNKZ1EAgYgmQfmNVamxyXVWHzK5s6DGhwP4J5o44cvXdoY7sRzhtpUeo";
    private static final String EC_UNCOMPRESSED = "6PfQu77ygVyJLZjfvMLyhLMQbYnu5uguoJJ4kMCLqWwPEdfpwANVS76gTX";
    private static final String EC_LOT = "6PgNBNNzDkKdhkT6uJntUXwwzQV8Rr2tZcbkDcuC9DZRsS6AtHts4Ypo1j";

    @Test
    public void testPublishedBitcoinBip38KeysRejected() {
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThree", NO_EC_UNCOMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("Satoshi", "6PRNFFkZc2NZ6dJqFfhRoFNMR9Lnyj7dYGrzdgXXVMXcxoKTePPX1dWByq"));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThree", NO_EC_COMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("Satoshi", "6PYLtMnXvfG3oJde97zRyLYFZCYizPU5T3LwgdYJz1fRhh16bU7u6PPmY7"));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThree", EC_UNCOMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("Satoshi", "6PfLGnQs6VZnrNpmVKfjotbnQuaJK4KZoPFrAjx1JMJUa1Ft8gnf5WxfKd"));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("MOLON LABE", EC_LOT));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("ΜΟΛΩΝ ΛΑΒΕ", "6PgGWtx25kUg8QWvwuJAgorN6k9FbE25rv5dMRwu5SKMnfpfVe5mar2ngH"));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThree", "6PfT3aPZr59YLHDHVz6yem2FuvXsUdDEPNYw7ArG11BFdpG31SqC9f9eUn"));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThree", "6PnRe5H6XHsMpnJyHtJNKQ8MtGBQhTwNqQpdHKwKLYWsZogTGQ1SiL39A7"));
    }

    @Test
    public void testNoCompressionNoECThisChain() throws GeneralSecurityException, UnsupportedEncodingException {
        ECKey key = ChainEncoding.keyFromPublishedWif("5KN7MzqK5wt2TP1fQCYyHBtDrXdJuXbUzm4A9rKAteGu3Qi5CVR");
        DumpedPrivateKey dumped = key.getPrivateKeyEncoded();
        String encrypted = BIP38.encrypt("TestingOneTwoThree", dumped);
        Assertions.assertEquals(dumped.toString(), BIP38.decrypt("TestingOneTwoThree", encrypted).toString());
        Assertions.assertEquals(dumped.toString(), BIP38.decrypt("Satoshi", BIP38.encrypt("Satoshi", dumped)).toString());
    }

    @Test
    public void testCompressionNoECThisChain() throws GeneralSecurityException, UnsupportedEncodingException {
        ECKey key = ChainEncoding.keyFromPublishedWif("L44B5gGEpqEDRS9vVPz7QT35jcBG2r3CZwSwQ4fCewXAhAhqGVpP");
        DumpedPrivateKey dumped = key.getPrivateKeyEncoded();
        String encrypted = BIP38.encrypt("TestingOneTwoThree", dumped);
        Assertions.assertEquals(dumped.toString(), BIP38.decrypt("TestingOneTwoThree", encrypted).toString());
        Assertions.assertEquals(dumped.toString(), BIP38.decrypt("Satoshi", BIP38.encrypt("Satoshi", dumped)).toString());
    }

    @Test
    public void testIncorrectPassphrase() {
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThreeFour", NO_EC_UNCOMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThreeFour", NO_EC_COMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("TestingOneTwoThreeFour", EC_UNCOMPRESSED));
        Assertions.assertThrows(InvalidPasswordException.class, () -> BIP38.decrypt("MOLON LABE!", EC_LOT));
    }

    @Test
    public void testMalformedKey() {
        byte[] valid = Base58.decodeChecked(NO_EC_UNCOMPRESSED);
        byte[] truncated = Arrays.copyOfRange(valid, 0, valid.length - 1);
        byte[] unknownType = Arrays.copyOf(valid, valid.length);
        unknownType[1] = 0x44;

        Assertions.assertThrows(GeneralSecurityException.class, () -> BIP38.decrypt("TestingOneTwoThree", Base58.encodeChecked(truncated)));
        Assertions.assertThrows(GeneralSecurityException.class, () -> BIP38.decrypt("TestingOneTwoThree", Base58.encodeChecked(unknownType)));
        Assertions.assertThrows(GeneralSecurityException.class, () -> BIP38.decryptNoEC("TestingOneTwoThree", truncated));
        Assertions.assertThrows(GeneralSecurityException.class, () -> BIP38.decryptEC("TestingOneTwoThree", truncated));
    }
}
