package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Bech32;
import com.sparrowwallet.drongo.protocol.ScriptType;

public class MlDsaAddress extends Address {
    public MlDsaAddress(byte[] program) {
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

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MLDSA_SINGLE;
    }

    @Override
    public String getOutputScriptDataType() {
        return "ML-DSA-44 program";
    }
}
