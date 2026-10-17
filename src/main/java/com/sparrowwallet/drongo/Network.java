package com.sparrowwallet.drongo;

import com.sparrowwallet.drongo.protocol.BlockHeader;
import com.sparrowwallet.drongo.protocol.HeaderCheckpoints;
import com.sparrowwallet.drongo.protocol.Sha256Hash;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public enum Network {
    MAINNET("mainnet", "Mainnet (not live)", "mainnet", 36, "F", 16, "7", "gfcn", "gfcnsp", "gfcnscan", "gfcnspend", ExtendedKey.Header.xprv, ExtendedKey.Header.xpub, 164, 4094),
    TESTNET("testnet", "Testnet3", "testnet3", 95, "f", 197, "2", "tgfcn", "tgfcnsp", "tgfcnscan", "tgfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 35332),
    REGTEST("regtest", "Regtest", "regtest", 95, "f", 197, "2", "gfcnrt", "gfcnrtsp", "gfcnrtscan", "gfcnrtspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 25443),
    SIGNET("signet", "Signet", "signet", 95, "f", 197, "2", "tgfcn", "tgfcnsp", "tgfcnscan", "tgfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 26332),
    TESTNET4("testnet4", "Testnet4", "testnet4", 95, "f", 197, "2", "tgfcn", "tgfcnsp", "tgfcnscan", "tgfcnspend", ExtendedKey.Header.tprv, ExtendedKey.Header.tpub, 223, 45332);

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
     * Blake2b from height 0 on MAINNET, TESTNET, TESTNET4, and SIGNET. REGTEST
     * is null because the node may still choose through -testactivationheight.
     */
    public Integer getBlake2bHeight() {
        return switch(this) {
            case MAINNET, TESTNET, TESTNET4, SIGNET -> 0;
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
            case MAINNET -> "01000080000000000000000000000000000000000000000000000000000000000000000046a324c53cf58bd64587350db497493bbbba66672ee8e12a3cc9cce13a81b1480065cd1dffff001e9abc6a00000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            case TESTNET -> "01000080000000000000000000000000000000000000000000000000000000000000000046a324c53cf58bd64587350db497493bbbba66672ee8e12a3cc9cce13a81b1483024ab6a30a8031c9febad9fe80000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            case REGTEST -> "01000080000000000000000000000000000000000000000000000000000000000000000046a324c53cf58bd64587350db497493bbbba66672ee8e12a3cc9cce13a81b1483324ab6affff7f2000824b00000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            case SIGNET -> "01000080000000000000000000000000000000000000000000000000000000000000000046a324c53cf58bd64587350db497493bbbba66672ee8e12a3cc9cce13a81b1483224ab6aae77031ef05ea500000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            case TESTNET4 -> "01000080000000000000000000000000000000000000000000000000000000000000000046a324c53cf58bd64587350db497493bbbba66672ee8e12a3cc9cce13a81b1483124ab6a30a8031cada787590a0000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
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
