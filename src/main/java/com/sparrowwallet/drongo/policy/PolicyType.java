package com.sparrowwallet.drongo.policy;

import com.sparrowwallet.drongo.protocol.ScriptType;

import static com.sparrowwallet.drongo.protocol.ScriptType.*;

public enum PolicyType {
    SINGLE_HD("Dilithium 87 single key", "Dilithium 87 single key", MLDSA87_SINGLE),
    MULTI_HD("Dilithium 87 multisig", "Dilithium 87 slots multisig", MLDSA87_MULTI),
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
