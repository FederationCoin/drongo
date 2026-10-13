package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Base58;
import com.sparrowwallet.drongo.protocol.Bech32;
import com.sparrowwallet.drongo.protocol.Script;
import com.sparrowwallet.drongo.protocol.ScriptType;

import java.util.Arrays;
import java.util.Locale;

public abstract class Address {
    protected final byte[] data;

    public Address(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public String getAddress() {
        return getAddress(Network.get());
    }

    public String getAddress(Network network) {
        return Base58.encodeChecked(getVersion(network), data);
    }

    public String toString() {
        return getAddress(Network.get());
    }

    public String toString(Network network) {
        return getAddress(network);
    }

    public int getVersion() {
        return getVersion(Network.get());
    }

    public abstract int getVersion(Network network);

    public abstract ScriptType getScriptType();

    public Script getOutputScript() {
        return getScriptType().getOutputScript(data);
    }

    public void requireSendable() throws InvalidAddressException {
        if(getScriptType().isParkedOnThisChain()) {
            throw new InvalidAddressException(ScriptType.TAPROOT_NOT_ENABLED_MESSAGE);
        }
        if(!(this instanceof MlDsaAddress) && !getScriptType().isOfferedForNewWallets()) {
            throw new InvalidAddressException("This chain pays Dilithium 87, Dilithium 44, or SegWit witness v0 programs");
        }
    }

    public byte[] getOutputScriptData() {
        return data;
    }

    public abstract String getOutputScriptDataType();

    public boolean equals(Object obj) {
        if(!(obj instanceof Address address)) {
            return false;
        }

        return Arrays.equals(data, address.data) && getVersion(Network.get()) == address.getVersion(Network.get());
    }

    public int hashCode() {
        return Arrays.hashCode(data) + getVersion(Network.get());
    }

    public static Address fromString(String address) throws InvalidAddressException {
        try {
            return fromString(Network.get(), address);
        } catch(InvalidAddressException e) {
            for(Network network : Network.canonicalValues()) {
                try {
                    fromString(network, address);
                    if(network != Network.getCanonical()) {
                        throw new InvalidAddressException("Provided " + network.getName() + " address invalid on configured " + Network.getCanonical().getName() + " network: " + address + ". Use a " + network.getName() + " configuration to use this address.");
                    }
                } catch(InvalidAddressException i) {
                    //ignore
                }
            }

            throw e;
        }
    }

    public static Address fromString(Network network, String address) throws InvalidAddressException {
        Exception nested = null;

        if(address != null) {
            if(network.hasP2PKHAddressPrefix(address) || network.hasP2SHAddressPrefix(address)) {
                throw new InvalidAddressException("This chain does not use base58 addresses");
            }

            if(address.toLowerCase(Locale.ROOT).startsWith(network.getBech32AddressHRP())) {
                try {
                    Bech32.Bech32Data data = Bech32.decode(address, com.sparrowwallet.drongo.protocol.ModernSig.BECH32_CHAR_LIMIT);
                    if(data.hrp.equals(network.getBech32AddressHRP())) {
                        if(data.encoding == Bech32.Encoding.BECH32) {
                            try {
                                byte[] scriptBytes = Bech32.convertBits(data.data, 0, data.data.length, 5, 8, false);
                                com.sparrowwallet.drongo.protocol.Script script = new com.sparrowwallet.drongo.protocol.Script(scriptBytes);
                                com.sparrowwallet.drongo.protocol.ModernSig.Single single = com.sparrowwallet.drongo.protocol.ModernSig.matchSingle(script);
                                if(single != null && single.algorithm() == com.sparrowwallet.drongo.protocol.ModernSig.ALGO_DILITHIUM) {
                                    if(single.param() == com.sparrowwallet.drongo.protocol.ModernSig.PARAM_DILITHIUM_44) {
                                        return new MlDsa44Address(single.fingerprint());
                                    }
                                    if(single.param() == com.sparrowwallet.drongo.protocol.ModernSig.PARAM_DILITHIUM_87) {
                                        return new MlDsa87Address(single.fingerprint());
                                    }
                                }
                                com.sparrowwallet.drongo.protocol.ModernSig.Policy policy = com.sparrowwallet.drongo.protocol.ModernSig.matchPolicy(script);
                                if(policy != null && policy.algorithm() == com.sparrowwallet.drongo.protocol.ModernSig.ALGO_DILITHIUM) {
                                    if(policy.param() == com.sparrowwallet.drongo.protocol.ModernSig.PARAM_DILITHIUM_44) {
                                        return new MlDsa44MultiAddress(scriptBytes);
                                    }
                                    if(policy.param() == com.sparrowwallet.drongo.protocol.ModernSig.PARAM_DILITHIUM_87) {
                                        return new MlDsa87MultiAddress(scriptBytes);
                                    }
                                }
                            } catch(Exception ignored) {
                                // Witness v0 payload is version + program, not a locking script.
                            }
                        }
                        int witnessVersion = data.data[0];
                        if(witnessVersion == 0) {
                            if(data.encoding != Bech32.Encoding.BECH32) {
                                throw new InvalidAddressException("Invalid address - witness version is 0 but encoding is " + data.encoding);
                            }

                            byte[] convertedProgram = Arrays.copyOfRange(data.data, 1, data.data.length);
                            byte[] witnessProgram = Bech32.convertBits(convertedProgram, 0, convertedProgram.length, 5, 8, false);
                            if(witnessProgram.length == 20) {
                                return new P2WPKHAddress(witnessProgram);
                            }
                            throw new InvalidAddressException("Witness v0 32-byte programs are not Dilithium addresses");
                        }
                        throw new InvalidAddressException("This chain does not use witness v1 or later addresses");
                    }
                } catch(InvalidAddressException e) {
                    throw e;
                } catch(Exception e) {
                    nested = e;
                }
            }
        }

        if(nested != null) {
            throw new InvalidAddressException("Could not parse invalid address " + address, nested);
        }

        throw new InvalidAddressException("Could not parse invalid address " + address);
    }
}
