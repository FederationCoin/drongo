package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Bech32;

public abstract class MlDsaAddress extends Address {
    protected MlDsaAddress(byte[] program) {
        super(program);
        if(program.length != 32) {
            throw new IllegalArgumentException("ML-DSA program is 32 bytes");
        }
    }

    @Override
    public int getVersion(Network network) {
        return 0;
    }

    @Override
    public String getAddress(Network network) {
        return Bech32.encode(network.getBech32AddressHRP(), getVersion(), data);
    }
}
