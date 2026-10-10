package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HeaderCheckpointsTest {
    private static final String SAMPLE_PIN = "00000000693067b0e6b440bc51450b9f3850561b07f6d3c021c54fbd6abb9763";

    @Test
    public void testMainnetCheckpoints() {
        HeaderCheckpoints checkpoints = Network.MAINNET.getHeaderCheckpoints();
        Assertions.assertEquals(0, checkpoints.getMaxHeight());
        Assertions.assertEquals(Network.MAINNET.getGenesisHash(), checkpoints.getHash(0));
        Assertions.assertEquals(0x1e00ffffL, checkpoints.getBitsAfter(0));
        Assertions.assertEquals(0, checkpoints.getPinnedHeightAtOrAbove(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getPinnedHeightAtOrAbove(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getHash(2015));
    }

    @Test
    public void testEveryNetworkResourceParses() {
        for(Network network : Network.values()) {
            HeaderCheckpoints checkpoints = network.getHeaderCheckpoints();
            int maxHeight = checkpoints.getMaxHeight();
            //Every pinned height is the last of a difficulty period, except regtest's genesis anchor
            Assertions.assertEquals(1, (maxHeight + 1) % HeaderChainState.RETARGET_INTERVAL, network.getName());
            Assertions.assertDoesNotThrow(() -> checkpoints.getHash(maxHeight), network.getName());
            Assertions.assertDoesNotThrow(() -> checkpoints.getBitsAfter(maxHeight), network.getName());
        }
    }

    @Test
    public void testEveryNetworkChainStateAnchorsAtTheLastPin() {
        for(Network network : Network.values()) {
            HeaderCheckpoints checkpoints = network.getHeaderCheckpoints();
            HeaderChainState chainState = checkpoints.newChainState();
            Assertions.assertEquals(checkpoints.getMaxHeight(), chainState.getHeight(), network.getName());
            Assertions.assertEquals(checkpoints.getHash(checkpoints.getMaxHeight()), chainState.getHash(), network.getName());
        }
    }

    @Test
    public void testPinnedHeightAtOrAbove() {
        HeaderCheckpoints checkpoints = Network.MAINNET.getHeaderCheckpoints();
        Assertions.assertEquals(0, checkpoints.getPinnedHeightAtOrAbove(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getPinnedHeightAtOrAbove(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getPinnedHeightAtOrAbove(-1));
    }

    @Test
    public void testUnpinnedHeightRejected() {
        HeaderCheckpoints checkpoints = Network.MAINNET.getHeaderCheckpoints();
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getHash(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getHash(2015));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getBitsAfter(2016));
    }

    @Test
    public void testRegtestAnchorsAtGenesis() {
        HeaderCheckpoints checkpoints = Network.REGTEST.getHeaderCheckpoints();
        Assertions.assertEquals(0, checkpoints.getMaxHeight());
        Assertions.assertEquals(Network.REGTEST.getGenesisHash(), checkpoints.getHash(0));
        Assertions.assertEquals(0x207fffffL, checkpoints.getBitsAfter(0));
        Assertions.assertEquals(0, checkpoints.getPinnedHeightAtOrAbove(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getPinnedHeightAtOrAbove(1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> checkpoints.getHash(2015));

        HeaderChainState chainState = checkpoints.newChainState();
        Assertions.assertEquals(0, chainState.getHeight());
        Assertions.assertEquals(Network.REGTEST.getGenesisHash(), chainState.getHash());
    }

    @Test
    public void testGenesisHeaders() {
        Assertions.assertEquals("000000a33f3356996804bbe8bee34a543d712c484b60a9dd9952f44183aaa590", Network.MAINNET.getGenesisHash().toString());
        Assertions.assertEquals("0000000002407773e5e3fc8289f00235f3dc7dd12a7eedbb60e16f38eff34f64", Network.TESTNET.getGenesisHash().toString());
        Assertions.assertEquals("751a65c1c058cce240ab96f65b5f1bc91783fcdeade3079f1240d64d1929bd30", Network.REGTEST.getGenesisHash().toString());
        Assertions.assertEquals("000001e7209f5488417bb4dcc98567233da38d48cb553bd649cd3dd87ae586da", Network.SIGNET.getGenesisHash().toString());
        Assertions.assertEquals("00000000017c20aa7d1f3af2bf025a0d6dcf8d0a6483f4d2ff171e8ff564c26e", Network.TESTNET4.getGenesisHash().toString());

        for(Network network : Network.values()) {
            Network.set(network);
            Assertions.assertTrue(network.getGenesisHeader().verifyProofOfWork(), network.getName());
            Assertions.assertEquals(Sha256Hash.ZERO_HASH, network.getGenesisHeader().getPrevBlockHash(), network.getName());
            Network.set(null);
        }
    }

    @Test
    public void testMalformedCheckpointsRejected() {
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN.substring(1) + " 1d00ffff"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN + " 1d00ffff extra"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN.replace('0', 'z') + " 1d00ffff"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(""));

        //A target that decodes as negative, as zero, or to a compact form other than the one written is not a consensus value
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN + " 1d80ffff"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN + " 00000000"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse(SAMPLE_PIN + " 1d0000ff"));

        Assertions.assertDoesNotThrow(() -> parse(SAMPLE_PIN + " 1e00ffff"));
    }

    @Test
    public void testMalformedResourceForOneNetworkLeavesOthersLoadable() {
        Assertions.assertThrows(IllegalStateException.class, () -> parse("nonsense"));
        Assertions.assertNotNull(Network.MAINNET.getHeaderCheckpoints().getHash(0));
    }

    private static HeaderCheckpoints parse(String content) throws IOException {
        return HeaderCheckpoints.parse(Network.MAINNET, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    @AfterEach
    public void tearDown() throws Exception {
        Network.set(null);
    }

    /**
     * Blake2b is from height 0, so the only valid pin is genesis.
     */
    @Test
    public void testOnlyGenesisIsPinned() {
        for(Network network : Network.values()) {
            Integer activationHeight = network.getBlake2bHeight();
            if(activationHeight != null) {
                Network.set(network);
                int maxHeight = HeaderCheckpoints.get(network).getMaxHeight();
                Assertions.assertEquals(0, maxHeight, network + " pins height " + maxHeight);
                Network.set(null);
            }
        }
    }
}
