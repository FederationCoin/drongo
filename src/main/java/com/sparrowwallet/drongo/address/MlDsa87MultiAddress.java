package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.protocol.ScriptType;

public class MlDsa87MultiAddress extends MlDsaAddress {
    public MlDsa87MultiAddress(byte[] program) {
        super(program);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MLDSA87_MULTI;
    }

    @Override
    public String getOutputScriptDataType() {
        return "ML-DSA-87 policy program";
    }
}
