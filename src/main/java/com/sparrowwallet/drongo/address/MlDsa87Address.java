package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.protocol.ScriptType;

public class MlDsa87Address extends MlDsaAddress {
    public MlDsa87Address(byte[] program) {
        super(program);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MLDSA87_SINGLE;
    }

    @Override
    public String getOutputScriptDataType() {
        return "ML-DSA-87 program";
    }
}
