package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Blake2b is from height 0 on this chain, with target shift 0. Dummy MAIN is not launched.
 *
 * Genesis is a v2 header. A v1 header at height 1 is the cheap SHA256d forgery the version check exists to refuse.
 */
public class MainnetActivationTest {
    private static final int ACTIVATION_HEIGHT = 0;

    @AfterEach
    public void tearDown() {
        Network.set(null);
    }

    @Test
    public void testActivationIsFromHeightZeroWithNoTargetShift() {
        Network.set(Network.MAINNET);
        Assertions.assertEquals(Integer.valueOf(ACTIVATION_HEIGHT), Network.get().getBlake2bHeight());
        Assertions.assertEquals(0, Network.get().getBlake2bTargetShift());
        Assertions.assertEquals(0x1e00ffffL, Network.get().applyBlake2bTargetShift(0x1e00ffffL));
        Assertions.assertEquals(0x1e00ffffL, HeaderChainState.applyBlake2bTargetShift(ACTIVATION_HEIGHT, 0x1e00ffffL));
    }

    @Test
    public void testGenesisIsAV2Header() {
        Network.set(Network.MAINNET);
        BlockHeader genesis = Network.MAINNET.getGenesisHeader();
        Assertions.assertTrue(genesis.isHeaderV2());
        Assertions.assertEquals(BlockHeader.V2_LENGTH, genesis.bitcoinSerialize().length);
        Assertions.assertTrue(genesis.verifyProofOfWork());
    }

    @Test
    public void testASha256dHeaderIsRefusedAboveGenesis() {
        Network.set(Network.MAINNET);

        BlockHeader genesis = Network.MAINNET.getGenesisHeader();
        HeaderChainState chainState = new HeaderChainState(0, genesis.getHash(), genesis.getDifficultyTarget());
        BlockHeader forged = new BlockHeader(1, genesis.getHash(), Sha256Hash.ZERO_HASH, null,
                genesis.getTime() + 600, genesis.getDifficultyTarget(), 0);
        Assertions.assertFalse(forged.isHeaderV2(), "the forgery is hashed with SHA256d");

        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(forged));
        Assertions.assertTrue(e.getMessage().contains("requires v2"), e.getMessage());
    }
}
