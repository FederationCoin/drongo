package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.BlockHeader;
import com.sparrowwallet.drongo.protocol.HeaderCheckpoints;
import com.sparrowwallet.drongo.protocol.Sha256Hash;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public enum Network {
    MAINNET("mainnet", "Mainnet (not live)", "mainnet", 36, "F", 16, "7", "fcn", "fcnsp", "fcnscan", "fcnspend", ExtendedKey.Header.xprv, ExtendedKey.Header.xpub, 164, 4094),
    TESTNET("testnet", "Testnet3", "testnet3", 95, "f", 197, "2", "tfcn", "tfcnsp", "tfcnscan", "tfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 35332),
    REGTEST("regtest", "Regtest", "regtest", 95, "f", 197, "2", "fcnrt", "fcnrtsp", "fcnrtscan", "fcnrtspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 25443),
    SIGNET("signet", "Signet", "signet", 95, "f", 197, "2", "tfcn", "tfcnsp", "tfcnscan", "tfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 26332),
    TESTNET4("testnet4", "Testnet4", "testnet4", 95, "f", 197, "2", "tfcn", "tfcnsp", "tfcnscan", "tfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 45332);

    public static final String BLOCK_HEIGHT_PROPERTY = "com.sparrowwallet.blockHeight";
    private static final Network[] CANONICAL_VALUES = new Network[]{MAINNET, TESTNET, REGTEST, SIGNET};

    Network(String name, String displayName, String home, int p2pkhAddressHeader, String p2pkhAddressPrefix, int p2shAddressHeader, String p2shAddressPrefix, String bech32AddressHrp, String spAddressHrp, String spScanKeyHrp, String spSpendKeyHrp, ExtendedKey.Header xprvHeader, ExtendedKey.Header xpubHeader, int dumpedPrivateKeyHeader, int defaultPort) {
        this.name = name;
        this.displayName = displayName;
        this.home = home;
        this.p2pkhAddressHeader = p2pkhAddressHeader;
        this.p2pkhAddressPrefix = p2pkhAddressPrefix;
        this.p2shAddressHeader = p2shAddressHeader;
        this.p2shAddressPrefix = p2shAddressPrefix;
        this.bech32AddressHrp = bech32AddressHrp;
        this.spAddressHrp = spAddressHrp;
        this.spScanKeyHrp = spScanKeyHrp;
        this.spSpendKeyHrp = spSpendKeyHrp;
        this.xprvHeader = xprvHeader;
        this.xpubHeader = xpubHeader;
        this.dumpedPrivateKeyHeader = dumpedPrivateKeyHeader;
        this.defaultPort = defaultPort;
    }

    private final String name;
    private final String displayName;
    private final String home;
    private final int p2pkhAddressHeader;
    private final String p2pkhAddressPrefix;
    private final int p2shAddressHeader;
    private final String p2shAddressPrefix;
    private final String bech32AddressHrp;
    private final String spAddressHrp;
    private final String spScanKeyHrp;
    private final String spSpendKeyHrp;
    private final ExtendedKey.Header xprvHeader;
    private final ExtendedKey.Header xpubHeader;
    private final int dumpedPrivateKeyHeader;
    private final int defaultPort;

    private static Network currentNetwork;
    private static final Map<Network, BlockHeader> GENESIS_HEADERS = new EnumMap<>(Network.class);

    public String getName() {
        return name;
    }

    public String getCapitalizedName() {
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    public String toDisplayString() {
        return displayName;
    }

    public String getHome() {
        return home;
    }

    public int getP2PKHAddressHeader() {
        return p2pkhAddressHeader;
    }

    public int getP2SHAddressHeader() {
        return p2shAddressHeader;
    }

    public String getBech32AddressHRP() {
        return bech32AddressHrp;
    }

    public String getSilentPaymentsAddressHrp() {
        return spAddressHrp;
    }

    public String getSilentPaymentsScanKeyHrp() {
        return spScanKeyHrp;
    }

    public String getSilentPaymentsSpendKeyHrp() {
        return spSpendKeyHrp;
    }

    public ExtendedKey.Header getXprvHeader() {
        return xprvHeader;
    }

    public ExtendedKey.Header getXpubHeader() {
        return xpubHeader;
    }

    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * The height the BLAKE2b hardfork activates at on this network, or null where this build ships no schedule for it.
     *
     * consensus.Blake2bHeight in the Knots chainparams. The schedule has moved more than once before a final release,
     * and each move replaced the chain that followed the old one, so this value is not to be trusted on its own: it is
     * cross checked against the connected node, and a disagreement declines rather than following either side.
     */
    public Integer getBlake2bHeight() {
        return switch(this) {
            case MAINNET, TESTNET, TESTNET4, SIGNET -> 1;
            case REGTEST -> null;    //regtest chooses its own through -testactivationheight
        };
    }

    /**
     * How far left the difficulty target is shifted at the activation height, being consensus.Blake2bTargetShift.
     *
     * The block at the activation height takes its parent's target shifted by this and capped at the proof of work
     * limit, then normal retargeting resumes. A one off allowance for the fact that no hardware mines the new
     * algorithm yet. Networks that do not pin one take the reference implementation's default.
     */
    public int getBlake2bTargetShift() {
        return 0;
    }

    /**
     * The target the block at the activation height is required to claim, given the one its parent claimed.
     * Follows ApplyBlake2bTargetShift: shift left, but never past the proof of work limit.
     */
    public long applyBlake2bTargetShift(long compactBits) {
        BigInteger target = Utils.decodeCompactBits(compactBits);
        BigInteger powLimit = getProofOfWorkLimit();
        BigInteger shifted = target.compareTo(powLimit.shiftRight(getBlake2bTargetShift())) > 0
                ? powLimit : target.shiftLeft(getBlake2bTargetShift());

        return Utils.encodeCompactBits(shifted);
    }

    /**
     * The maximum (easiest) allowed difficulty target for this network, decoded from the consensus powLimit compact bits.
     */
    public BigInteger getProofOfWorkLimit() {
        if(this == REGTEST) {
            return Utils.decodeCompactBits(0x207fffffL);
        } else if(this == SIGNET) {
            return Utils.decodeCompactBits(0x1e0377aeL);
        }

        return Utils.decodeCompactBits(0x1e00ffffL);
    }

    /**
     * The network's genesis block header, parsed from its compiled-in serialization on first use.
     */
    public BlockHeader getGenesisHeader() {
        synchronized(GENESIS_HEADERS) {
            return GENESIS_HEADERS.computeIfAbsent(this, network -> new BlockHeader(Utils.hexToBytes(network.getGenesisHeaderHex())));
        }
    }

    public Sha256Hash getGenesisHash() {
        return getGenesisHeader().getHash();
    }

    private String getGenesisHeaderHex() {
        return switch(this) {
            case MAINNET -> "0100000000000000000000000000000000000000000000000000000000000000000000007e44f3e38c51497d5c1f61b86fdf2d8b13c6b7748f8e2c1575d841714c62acaf0065cd1dffff001e20bb3600";
            case TESTNET -> "0100000000000000000000000000000000000000000000000000000000000000000000009f0e8be762d076368e0be351e9a7b11f94e68629b073971391369f47c86e7a6581a1a06affff001ece88ba04";
            case REGTEST -> "01000000000000000000000000000000000000000000000000000000000000000000000009f6a971dd15f9873483034fc0c3856db270b057193fccd84eb4394d64b424aa84a1a06affff7f2000000000";
            case SIGNET -> "010000000000000000000000000000000000000000000000000000000000000000000000c5d7d34755da3bbd31fe60a5cde01b02e855a9c1fa2490d10752c4a1d067a51083a1a06aae77031e12236f00";
            case TESTNET4 -> "010000000000000000000000000000000000000000000000000000000000000000000000310e3daed692cc261372098ecc048292e91ada612d547e3a29f0f6978ec7eb4682a1a06affff001eca120701";
        };
    }

    /**
     * The pinned header hashes compiled in for this network, one per difficulty period. Regtest has none, anchoring at its genesis header instead.
     */
    public HeaderCheckpoints getHeaderCheckpoints() {
        return HeaderCheckpoints.get(this);
    }

    public boolean hasP2PKHAddressPrefix(String address) {
        for(String prefix : p2pkhAddressPrefix.split("")) {
            if(address.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasP2SHAddressPrefix(String address) {
        return address.startsWith(p2shAddressPrefix);
    }

    public static Network get() {
        if(currentNetwork == null) {
            currentNetwork = MAINNET;
        }

        return currentNetwork;
    }

    public static Network getCanonical() {
        return get() == TESTNET4 ? TESTNET : get();
    }

    public static Network[] canonicalValues() {
        return CANONICAL_VALUES;
    }

    public static void set(Network network) {
        if(currentNetwork != null && network != currentNetwork && !isTest()) {
            throw new IllegalStateException("Network already set to " + currentNetwork.getName());
        }

        currentNetwork = network;
    }

    private static boolean isTest() {
        return System.getProperty("org.gradle.test.worker") != null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
