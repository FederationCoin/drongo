package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BitcoinUnitTest {
    @Test
    public void labelsAreFcnAndTokens() {
        Assertions.assertEquals("Auto", BitcoinUnit.AUTO.getLabel());
        Assertions.assertEquals("FCN", BitcoinUnit.BTC.getLabel());
        Assertions.assertEquals("tokens", BitcoinUnit.SATOSHIS.getLabel());
        Assertions.assertEquals("FCN", BitcoinUnit.BTC.toString());
        Assertions.assertEquals("tokens", BitcoinUnit.SATOSHIS.toString());
    }

    @Test
    public void btcConvertsWholeCoinsToTokens() {
        Assertions.assertEquals(Transaction.SATOSHIS_PER_BITCOIN, BitcoinUnit.BTC.getSatsValue(1.0));
        Assertions.assertEquals(1.0, BitcoinUnit.BTC.getValue(Transaction.SATOSHIS_PER_BITCOIN));
        Assertions.assertEquals(2_500_000L, BitcoinUnit.BTC.getSatsValue(0.025));
    }

    @Test
    public void tokensAreOneToOne() {
        Assertions.assertEquals(42L, BitcoinUnit.SATOSHIS.getSatsValue(42.9));
        Assertions.assertEquals(42.0, BitcoinUnit.SATOSHIS.getValue(42L));
    }

    @Test
    public void autoCannotConvert() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> BitcoinUnit.AUTO.getSatsValue(1.0));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> BitcoinUnit.AUTO.getValue(1L));
    }
}
