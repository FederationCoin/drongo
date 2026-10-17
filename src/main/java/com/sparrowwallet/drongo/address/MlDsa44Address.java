package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.protocol.ScriptType;

public class MlDsa44Address extends MlDsaAddress {
    public MlDsa44Address(byte[] program) {
        super(program);
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
