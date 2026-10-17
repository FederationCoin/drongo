package com.sparrowwallet.drongo.wallet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletModelSupportTest {
    @Test
    public void softwareOfferedHardwareOff() {
        Assertions.assertTrue(WalletModel.SEED.supportsThisChain());
        Assertions.assertTrue(WalletModel.SPARROW.supportsThisChain());
        Assertions.assertTrue(WalletModel.ELECTRUM.supportsThisChain());
        Assertions.assertFalse(WalletModel.COLDCARD.supportsThisChain());
        Assertions.assertFalse(WalletModel.TREZOR_T.supportsThisChain());
        Assertions.assertFalse(WalletModel.TAPSIGNER.supportsThisChain());
        Assertions.assertFalse(WalletModel.LEDGER_NANO_X.supportsThisChain());
    }
}
