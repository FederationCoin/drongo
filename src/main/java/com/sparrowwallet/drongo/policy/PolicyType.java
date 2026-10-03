package com.sparrowwallet.drongo.policy;

import com.sparrowwallet.drongo.protocol.ScriptType;

import static com.sparrowwallet.drongo.protocol.ScriptType.*;

public enum PolicyType {
    SINGLE_HD("ML-DSA-44 single key", "ML-DSA-44 single key", MLDSA_SINGLE),
    MULTI_HD("ML-DSA-44 multisig", "ML-DSA-44 slots multisig", MLDSA_MULTI),
    // Heritage: silent payments / Taproot. Not a spend on this chain.
    SINGLE_SP("Single Signature SP", "Single Signature SP (Silent Payments)", P2TR);

    private final String name;
    private final String description;
    private final ScriptType defaultScriptType;

    PolicyType(String name, String description, ScriptType defaultScriptType) {
        this.name = name;
        this.description = description;
        this.defaultScriptType = defaultScriptType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ScriptType getDefaultScriptType() {
        return defaultScriptType;
    }

    public boolean isOfferedForNewWallets() {
        return this != SINGLE_SP;
    }

    @Override
    public String toString() {
        return name;
    }
}
