package com.sparrowwallet.drongo.policy;

import com.sparrowwallet.drongo.protocol.ScriptType;

import static com.sparrowwallet.drongo.protocol.ScriptType.*;

public enum PolicyType {
    SINGLE_MLDSA87("Dilithium 87 single key", "Dilithium 87 single key", MLDSA87_SINGLE) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitSingleMldsa87();
        }

        @Override
        public boolean isSingleKey() {
            return true;
        }
    },
    MULTI_MLDSA87("Dilithium 87 multisig", "Dilithium 87 slots multisig", MLDSA87_MULTI) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitMultiMldsa87();
        }

        @Override
        public boolean isHomogeneousMultisig() {
            return true;
        }

        @Override
        public boolean isOfferedForNewWallets() {
            return false;
        }
    },
    SINGLE_MLDSA44("Dilithium 44 single key", "Dilithium 44 single key", MLDSA_SINGLE) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitSingleMldsa44();
        }

        @Override
        public boolean isSingleKey() {
            return true;
        }
    },
    MULTI_MLDSA44("Dilithium 44 multisig", "Dilithium 44 slots multisig", MLDSA_MULTI) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitMultiMldsa44();
        }

        @Override
        public boolean isHomogeneousMultisig() {
            return true;
        }

        @Override
        public boolean isOfferedForNewWallets() {
            return false;
        }
    },
    SINGLE_SEGWIT("SegWit single key", "Native SegWit single key", P2WPKH) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitSingleSegwit();
        }

        @Override
        public boolean isSingleKey() {
            return true;
        }
    },
    MULTI_SEGWIT("SegWit multisig", "Native SegWit slots multisig", P2WSH) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitMultiSegwit();
        }

        @Override
        public boolean isHomogeneousMultisig() {
            return true;
        }

        @Override
        public boolean isOfferedForNewWallets() {
            return false;
        }
    },
    SINGLE_SP("Single Signature SP", "Single Signature SP (Silent Payments)", P2TR) {
        @Override
        public <T> T accept(PolicyVisitor<T> visitor) {
            return visitor.visitSingleSp();
        }

        @Override
        public boolean isSingleKey() {
            return true;
        }

        @Override
        public boolean isOfferedForNewWallets() {
            return false;
        }

        @Override
        public boolean isSilentPayment() {
            return true;
        }
    };

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

    public abstract <T> T accept(PolicyVisitor<T> visitor);

    public boolean isSingleKey() {
        return false;
    }

    public boolean isHomogeneousMultisig() {
        return false;
    }

    public boolean isSilentPayment() {
        return false;
    }

    public boolean isOfferedForNewWallets() {
        return true;
    }

    public boolean isSegwit() {
        return accept(new PolicyVisitor<Boolean>() {
            @Override
            public Boolean visitSingleMldsa87() {
                return false;
            }

            @Override
            public Boolean visitMultiMldsa87() {
                return false;
            }

            @Override
            public Boolean visitSingleMldsa44() {
                return false;
            }

            @Override
            public Boolean visitMultiMldsa44() {
                return false;
            }

            @Override
            public Boolean visitSingleSegwit() {
                return true;
            }

            @Override
            public Boolean visitMultiSegwit() {
                return true;
            }

            @Override
            public Boolean visitSingleSp() {
                return false;
            }
        });
    }

    public static PolicyType forScript(ScriptType scriptType, boolean multisig) {
        for(PolicyType type : scriptType.getAllowedPolicyTypes()) {
            if(multisig ? type.isHomogeneousMultisig() : type.isSingleKey()) {
                return type;
            }
        }
        throw new IllegalArgumentException("No " + (multisig ? "multisig" : "single-key") + " policy for script type " + scriptType);
    }

    @Override
    public String toString() {
        return name;
    }
}
