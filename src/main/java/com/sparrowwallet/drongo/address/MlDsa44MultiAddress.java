package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.protocol.ScriptType;

public class MlDsa44MultiAddress extends MlDsaAddress {
    public MlDsa44MultiAddress(byte[] program) {
        super(program);
    }

    @Override
    public ScriptType getScriptType() {
        return ScriptType.MLDSA_MULTI;
    }

    @Override
    public String getOutputScriptDataType() {
        return "ML-DSA-44 policy program";
    }
}
