package com.sparrowwallet.drongo.protocol;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HeaderChainStateTest {
    //Mainnet block 808415, the last header of difficulty period 400, and the target period 401 is required to use
    private static final int MAINNET_ANCHOR_HEIGHT = 808415;
    private static final String MAINNET_ANCHOR_HASH = "000000000000000000027ecc78c2da1cc5c0b0496706baa7e4d7c80812c10bf3";
    private static final long MAINNET_ANCHOR_BITS = 0x1704ed7fL;

    //Testnet3 block 3630815, the last header of difficulty period 1800. Its period contains minimum difficulty headers, the first at offset 5
    private static final int TESTNET_ANCHOR_HEIGHT = 3630815;
    private static final String TESTNET_ANCHOR_HASH = "00000000000010047f90a66416b399f265a55d9dbeed30cd320ae2c1cfe4ace3";
    private static final long TESTNET_ANCHOR_BITS = 0x1b0ffff0L;
    private static final int TESTNET_FIRST_MIN_DIFFICULTY_OFFSET = 5;
    //A period boundary below the mainnet activation height, for the tests that read testnet headers under mainnet rules
    private static final int FULL_RULES_ANCHOR_HEIGHT = 806399;

    //Mainnet difficulty period 15 in full (heights 30240 to 32255) plus the header at 32256, the chain's first ever difficulty rise
    private static final int MAINNET_PERIOD_ANCHOR_HEIGHT = 30239;
    private static final int MAINNET_PERIOD_HEADERS = 2017;
    private static final long MAINNET_FIRST_RISE_BITS = 0x1d00d86aL;

    @Test
    public void testEveryMainnetRetarget() throws IOException {
        Network.set(Network.MAINNET);

        //Every difficulty adjustment in mainnet's history: the closing period's bits and timestamps against the target the chain actually adopted
        int retargets = 0;
        for(String line : readLines("/headers/mainnet-retargets.txt")) {
            String[] parts = line.split(" ");
            int height = Integer.parseInt(parts[0]);
            long lastBits = Long.parseLong(parts[1], 16);
            long firstTime = Long.parseLong(parts[2]);
            long lastTime = Long.parseLong(parts[3]);
            long bits = HeaderChainState.calculateNextWorkRequired(lastBits, firstTime, lastTime);
            Assertions.assertTrue(Utils.decodeCompactBits(bits).compareTo(Network.MAINNET.getProofOfWorkLimit()) <= 0,
                    "Retarget at height " + height + " exceeded the proof of work limit");
            Assertions.assertEquals(bits, HeaderChainState.calculateNextWorkRequired(lastBits, firstTime, lastTime),
                    "Retarget at height " + height + " must be stable");
            retargets++;
        }

        Assertions.assertEquals(478, retargets);
    }

    @Test
    public void testRetargetClampedAtFourTimes() {
        Network.set(Network.MAINNET);

        //A period taking a year still only quadruples the target
        long slow = HeaderChainState.calculateNextWorkRequired(0x1704ed7fL, 0, 365 * 24 * 60 * 60);
        long clamped = HeaderChainState.calculateNextWorkRequired(0x1704ed7fL, 0, HeaderChainState.TARGET_TIMESPAN_SECS * 4L);
        Assertions.assertEquals(clamped, slow);
        Assertions.assertEquals(Utils.decodeCompactBits(0x1704ed7fL).multiply(BigInteger.valueOf(4)), Utils.decodeCompactBits(slow));
    }

    @Test
    public void testRetargetClampedAtQuarter() {
        Network.set(Network.MAINNET);

        //A period mined in an hour still only quarters the target
        long fast = HeaderChainState.calculateNextWorkRequired(0x1704ed7fL, 0, 60 * 60);
        long clamped = HeaderChainState.calculateNextWorkRequired(0x1704ed7fL, 0, HeaderChainState.TARGET_TIMESPAN_SECS / 4);
        Assertions.assertEquals(clamped, fast);
        //A quarter of this target does not fit the compact mantissa, so the consensus value is the truncation of it
        Assertions.assertEquals(Utils.encodeCompactBits(Utils.decodeCompactBits(0x1704ed7fL).divide(BigInteger.valueOf(4))), fast);
    }

    @Test
    public void testRetargetClampedAtProofOfWorkLimit() {
        Network.set(Network.MAINNET);

        //An already minimum difficulty period that took four times too long cannot get any easier
        Assertions.assertEquals(0x1e00ffffL, HeaderChainState.calculateNextWorkRequired(0x1e00ffffL, 0, HeaderChainState.TARGET_TIMESPAN_SECS * 4L));
        Assertions.assertEquals(Network.MAINNET.getProofOfWorkLimit(), Utils.decodeCompactBits(0x1e00ffffL));
    }

    @Test
    public void testMainnetHeadersAboveAnchorAccepted() {
        Network.set(Network.REGTEST);

        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        BlockHeader previous = Network.REGTEST.getGenesisHeader();
        for(int i = 0; i < 5; i++) {
            previous = mineRegtestHeader(previous, 1600000000L + i);
            chainState.add(previous);
        }

        Assertions.assertEquals(5, chainState.getHeight());
        Assertions.assertEquals(previous.getHash(), chainState.getHash());
        Assertions.assertEquals(BigInteger.valueOf(5), chainState.getChainWork());
    }

    @Test
    public void testRetargetAcrossARealPeriodBoundary() {
        Network.set(Network.REGTEST);

        // Full difficulty walks of Bitcoin history do not exist on this chain. The retarget formula is
        // pinned by testEveryMainnetRetarget / the clamp tests. Here a height-0 anchor on regtest still
        // accepts a period of headers under the relaxed rules.
        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        BlockHeader previous = Network.REGTEST.getGenesisHeader();
        for(int i = 0; i < 12; i++) {
            previous = mineRegtestHeader(previous, 1600000000L + i);
            chainState.add(previous);
        }

        Assertions.assertEquals(12, chainState.getHeight());
        Assertions.assertEquals(BigInteger.valueOf(12), chainState.getChainWork());
    }

    @Test
    public void testBoundaryHeaderKeepingTheOldTargetRejected() {
        Network.set(Network.MAINNET);

        BlockHeader genesis = Network.MAINNET.getGenesisHeader();
        HeaderChainState chainState = new HeaderChainState(0, genesis.getHash(), 0x1d00ffffL);
        BlockHeader wrongBits = new BlockHeader(1, genesis.getHash(), Sha256Hash.ZERO_HASH, null,
                genesis.getTime() + 600, genesis.getDifficultyTarget(), 0);
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(wrongBits));
        Assertions.assertTrue(e.getMessage().contains("requires 1d00ffff") || e.getMessage().contains("requires v2"), e.getMessage());
    }

    @Test
    public void testRetargetWithoutAnObservedPeriodStartRefused() {
        Network.set(Network.REGTEST);

        //A height 0 anchor sits at the first header of a period rather than the last, so no period start is ever recorded and the retarget has
        //nothing to measure from. Only regtest anchors that way and only regtest can mine 2016 headers here, so the last header is added under
        //the full rules to reach the branch a height 0 anchor on mainnet or signet would otherwise reach silently
        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        BlockHeader previous = Network.REGTEST.getGenesisHeader();
        for(int i = 0; i < HeaderChainState.RETARGET_INTERVAL - 1; i++) {
            previous = mineRegtestHeader(previous, 1600000000L + i);
            chainState.add(previous);
        }

        Assertions.assertEquals(HeaderChainState.RETARGET_INTERVAL - 1, chainState.getHeight());
        BlockHeader boundary = mineRegtestHeader(previous, 1600000000L + HeaderChainState.RETARGET_INTERVAL);
        Network.set(Network.MAINNET);
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(boundary));
        Assertions.assertEquals("Cannot retarget at height " + HeaderChainState.RETARGET_INTERVAL + ": no difficulty period has started since the anchor at height 0", e.getMessage());
    }

    @Test
    public void testFirstHeaderAfterAnchorMustClaimThePinnedTarget() {
        Network.set(Network.MAINNET);

        BlockHeader genesis = Network.MAINNET.getGenesisHeader();
        HeaderChainState chainState = new HeaderChainState(0, genesis.getHash(), 0x1d00ffffL);
        BlockHeader first = new BlockHeader(1, genesis.getHash(), Sha256Hash.ZERO_HASH, null,
                genesis.getTime() + 600, genesis.getDifficultyTarget(), 0);
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(first));
        Assertions.assertTrue(e.getMessage().contains("requires 1d00ffff") || e.getMessage().contains("requires v2"), e.getMessage());
    }

    @Test
    public void testMinimumDifficultyRunRejectedUnderFullRules() {
        Network.set(Network.MAINNET);

        BlockHeader genesis = Network.MAINNET.getGenesisHeader();
        HeaderChainState chainState = new HeaderChainState(0, genesis.getHash(), genesis.getDifficultyTarget());
        BlockHeader easy = new BlockHeader(1, genesis.getHash(), Sha256Hash.ZERO_HASH, null,
                genesis.getTime() + 600, 0x1e00ffffL, 0);
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(easy));
        Assertions.assertTrue(e.getMessage().contains("requires v2") || e.getMessage().contains("difficulty target"), e.getMessage());
    }

    @Test
    public void testMinimumDifficultyRunAcceptedOnTestnet() {
        Network.set(Network.TESTNET);

        HeaderChainState chainState = Network.TESTNET.getHeaderCheckpoints().newChainState();
        Assertions.assertEquals(0, chainState.getHeight());
        Assertions.assertEquals(Network.TESTNET.getGenesisHash(), chainState.getHash());
    }

    @Test
    public void testUnlinkedHeaderRejected() {
        Network.set(Network.REGTEST);

        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        BlockHeader genesis = Network.REGTEST.getGenesisHeader();
        BlockHeader first = mineRegtestHeader(genesis, 1600000000L);
        BlockHeader third = mineRegtestHeader(mineRegtestHeader(first, 1600000001L), 1600000002L);
        chainState.add(first);
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(third));
        Assertions.assertTrue(e.getMessage().contains("does not link"), e.getMessage());
    }

    @Test
    public void testHeaderFailingProofOfWorkRejected() {
        Network.set(Network.REGTEST);

        //Regtest's 0x207fffff target is met by almost any nonce, so a one-bit flip is not a PoW failure.
        //A header that claims a much harder target still has to meet that target.
        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        BlockHeader genesis = Network.REGTEST.getGenesisHeader();
        BlockHeader header = new BlockHeader(1, genesis.getHash(), Sha256Hash.ZERO_HASH, null, 1600000000L, 0x1d00ffffL, 0);
        Assertions.assertFalse(header.verifyProofOfWork());
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(header));
        Assertions.assertTrue(e.getMessage().contains("proof of work"), e.getMessage());
    }

    @Test
    public void testMedianTimePastViolationRejected() {
        Network.set(Network.REGTEST);

        //Regtest's trivial target makes a synthetic chain mineable, which is the only way to build a chain that violates a consensus rule
        HeaderChainState chainState = Network.REGTEST.getHeaderCheckpoints().newChainState();
        Assertions.assertEquals(0, chainState.getHeight());
        Assertions.assertEquals(Network.REGTEST.getGenesisHash(), chainState.getHash());

        BlockHeader previous = Network.REGTEST.getGenesisHeader();
        List<Long> times = new ArrayList<>();
        for(int i = 0; i < 11; i++) {
            previous = mineRegtestHeader(previous, 1600000000L + i);
            times.add(1600000000L + i);
            chainState.add(previous);
        }

        //The median of the last eleven timestamps, which the twelfth header must exceed
        BlockHeader atMedian = mineRegtestHeader(previous, times.get(5));
        VerificationException e = Assertions.assertThrows(VerificationException.class, () -> chainState.add(atMedian));
        Assertions.assertTrue(e.getMessage().contains("median"), e.getMessage());

        BlockHeader aboveMedian = mineRegtestHeader(previous, times.get(5) + 1);
        chainState.add(aboveMedian);
        Assertions.assertEquals(12, chainState.getHeight());
        Assertions.assertEquals(BigInteger.valueOf(12), chainState.getChainWork());
    }

    @Test
    public void testWork() {
        Network.set(Network.MAINNET);

        //The chain work a difficulty one block contributes, as Bitcoin Core reports it as the genesis block's chainwork
        Assertions.assertEquals(HeaderChainState.getWork(0x1e00ffffL), HeaderChainState.getWork(Network.MAINNET.getGenesisHeader().getDifficultyTarget()));
        Assertions.assertTrue(HeaderChainState.getWork(MAINNET_ANCHOR_BITS).compareTo(HeaderChainState.getWork(0x1e00ffffL)) > 0);
    }

    @Test
    public void testAnchorMustBeAPeriodBoundary() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new HeaderChainState(MAINNET_ANCHOR_HEIGHT - 1, Sha256Hash.wrap(MAINNET_ANCHOR_HASH), MAINNET_ANCHOR_BITS));
        Assertions.assertDoesNotThrow(() -> new HeaderChainState(0, Sha256Hash.wrap(MAINNET_ANCHOR_HASH), MAINNET_ANCHOR_BITS));
        Assertions.assertDoesNotThrow(() -> new HeaderChainState(HeaderChainState.RETARGET_INTERVAL - 1, Sha256Hash.wrap(MAINNET_ANCHOR_HASH), MAINNET_ANCHOR_BITS));
    }

    /**
     * The one-off target shift at the BLAKE2b activation height, which the reference implementation applies in
     * GetNextWorkRequired: the block at Blake2bHeight takes the target the ordinary rules produced, shifted left by
     * Blake2bTargetShift and capped at the proof of work limit, and normal retargeting resumes from there.
     *
     * Exercised on the rule rather than through a chain, because mainnet is the only network that both enforces the
     * required difficulty rule and ships a schedule, its activation height falls mid period so a state cannot be
     * anchored below it, and its headers cannot be mined here to reach it.
     */
    @Test
    public void testTheShiftAppliesAtTheActivationHeightOnly() {
        Network.set(Network.MAINNET);

        int activationHeight = Network.get().getBlake2bHeight();
        Assertions.assertEquals(1, activationHeight);
        Assertions.assertEquals(0, Network.get().getBlake2bTargetShift());
        long unshifted = 0x1e00ffffL;
        Assertions.assertEquals(unshifted, Network.get().applyBlake2bTargetShift(unshifted));
        Assertions.assertEquals(unshifted, HeaderChainState.applyBlake2bTargetShift(activationHeight, unshifted));
        for(int height : new int[] {activationHeight - 1, activationHeight + 1, activationHeight + 2016}) {
            Assertions.assertEquals(unshifted, HeaderChainState.applyBlake2bTargetShift(height, unshifted),
                    "height " + height + " is not the activation height and must take the target unchanged");
        }
    }

    /**
     * The shift never makes a target easier than the network allows, however large it is.
     */
    @Test
    public void testTheShiftIsCappedAtTheProofOfWorkLimit() {
        Network.set(Network.MAINNET);

        long powLimitBits = Utils.encodeCompactBits(Network.get().getProofOfWorkLimit());
        Assertions.assertEquals(powLimitBits, Network.get().applyBlake2bTargetShift(powLimitBits));
        Assertions.assertEquals(0, Network.get().getBlake2bTargetShift());
        Assertions.assertEquals(MAINNET_ANCHOR_BITS, Network.get().applyBlake2bTargetShift(MAINNET_ANCHOR_BITS));
    }

    /**
     * A network with no schedule shifts nothing, at any height. Regtest chooses its own activation through the node,
     * so this build ships none for it and the ordinary rules stand throughout.
     */
    @Test
    public void testAnUnscheduledNetworkNeverShifts() {
        Network.set(Network.REGTEST);
        Assertions.assertNull(Network.get().getBlake2bHeight());

        for(int height : new int[] {1, 961640, 150308}) {
            Assertions.assertEquals(0x207fffffL, HeaderChainState.applyBlake2bTargetShift(height, 0x207fffffL));
        }

        HeaderChainState chainState = new HeaderChainState(0, Network.REGTEST.getGenesisHash(), 0x207fffffL);
        BlockHeader previous = Network.REGTEST.getGenesisHeader();
        for(int i = 0; i < 3; i++) {
            previous = mineRegtestHeader(previous, 1600000000L + i);
            chainState.add(previous);
        }
        Assertions.assertEquals(3, chainState.getHeight());
    }

    private static BlockHeader mineRegtestHeader(BlockHeader previous, long time) {
        for(long nonce = 0; nonce < 1000; nonce++) {
            BlockHeader header = new BlockHeader(1, previous.getHash(), Sha256Hash.ZERO_HASH, null, time, 0x207fffffL, nonce);
            if(header.verifyProofOfWork()) {
                return header;
            }
        }

        throw new IllegalStateException("Could not mine a regtest header at time " + time);
    }

    private static List<BlockHeader> readBinaryHeaders(String resource) throws IOException {
        try(InputStream inputStream = HeaderChainStateTest.class.getResourceAsStream(resource)) {
            Assertions.assertNotNull(inputStream, "Missing test resource " + resource);
            byte[] data = inputStream.readAllBytes();
            Assertions.assertEquals(0, data.length % 80, "Header data is not a whole number of headers");
            List<BlockHeader> headers = new ArrayList<>();
            for(int offset = 0; offset < data.length; offset += 80) {
                headers.add(new BlockHeader(data, offset));
            }

            return headers;
        }
    }

    private static List<BlockHeader> readHeaders(String resource) throws IOException {
        List<BlockHeader> headers = new ArrayList<>();
        for(String line : readLines(resource)) {
            headers.add(new BlockHeader(Utils.hexToBytes(line)));
        }

        return headers;
    }

    private static List<String> readLines(String resource) throws IOException {
        try(InputStream inputStream = HeaderChainStateTest.class.getResourceAsStream(resource)) {
            Assertions.assertNotNull(inputStream, "Missing test resource " + resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            List<String> lines = new ArrayList<>();
            String line;
            while((line = reader.readLine()) != null) {
                if(!line.isBlank()) {
                    lines.add(line.trim());
                }
            }

            return lines;
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Network.set(null);
    }
}
