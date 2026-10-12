package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoinUnitTest {
    @Test
    public void labelsAreGfcnAndTokens() {
        Assertions.assertEquals("Auto", CoinUnit.AUTO.getLabel());
        Assertions.assertEquals("GFCN", CoinUnit.GFCN.getLabel());
        Assertions.assertEquals("tokens", CoinUnit.TOKENS.getLabel());
        Assertions.assertEquals("GFCN", CoinUnit.GFCN.toString());
        Assertions.assertEquals("tokens", CoinUnit.TOKENS.toString());
    }

    @Test
    public void gfcnConvertsWholeCoinsToTokens() {
        Assertions.assertEquals(Transaction.TOKENS_PER_COIN, CoinUnit.GFCN.getTokensValue(1.0));
        Assertions.assertEquals(1.0, CoinUnit.GFCN.getValue(Transaction.TOKENS_PER_COIN));
        Assertions.assertEquals(2_500_000L, CoinUnit.GFCN.getTokensValue(0.025));
    }

    @Test
    public void tokensAreOneToOne() {
        Assertions.assertEquals(42L, CoinUnit.TOKENS.getTokensValue(42.9));
        Assertions.assertEquals(42.0, CoinUnit.TOKENS.getValue(42L));
    }

    @Test
    public void autoCannotConvert() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> CoinUnit.AUTO.getTokensValue(1.0));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> CoinUnit.AUTO.getValue(1L));
    }
}
