package com.sparrowwallet.drongo.policy;

import com.sparrowwallet.drongo.protocol.ScriptType;
import com.sparrowwallet.drongo.wallet.Keystore;
import com.sparrowwallet.drongo.wallet.Persistable;

import java.util.List;

import static com.sparrowwallet.drongo.protocol.ScriptType.MULTISIG;

public class Policy extends Persistable {
    private static final String DEFAULT_NAME = "Default";

    private String name;
    private Miniscript miniscript;

    public Policy(Miniscript miniscript) {
        this(DEFAULT_NAME, miniscript);
    }

    public Policy(String name, Miniscript miniscript) {
        this.name = name;
        this.miniscript = miniscript;
    }

    public String getName() {
        return name;
    }

    public Miniscript getMiniscript() {
        return miniscript;
    }

    public void setMiniscript(Miniscript miniscript) {
        this.miniscript = miniscript;
    }

    public int getNumSignaturesRequired() {
        return getMiniscript().getNumSignaturesRequired();
    }

    public static Policy getPolicy(PolicyType policyType, ScriptType scriptType, List<Keystore> keystores, Integer threshold) {
        return policyType.accept(new PolicyVisitor<Policy>() {
            @Override
            public Policy visitSingleMldsa87() {
                return singleKey(scriptType, keystores);
            }

            @Override
            public Policy visitSingleMldsa44() {
                return singleKey(scriptType, keystores);
            }

            @Override
            public Policy visitSingleSegwit() {
                return singleKey(scriptType, keystores);
            }

            @Override
            public Policy visitMultiMldsa87() {
                return homogeneousMulti(scriptType, keystores, threshold);
            }

            @Override
            public Policy visitMultiMldsa44() {
                return homogeneousMulti(scriptType, keystores, threshold);
            }

            @Override
            public Policy visitMultiSegwit() {
                return homogeneousMulti(scriptType, keystores, threshold);
            }

            @Override
            public Policy visitSingleSp() {
                return new Policy(new Miniscript("sp(" + keystores.get(0).getScriptName() + ")"));
            }
        });
    }

    private static Policy singleKey(ScriptType scriptType, List<Keystore> keystores) {
        return new Policy(new Miniscript(scriptType.getDescriptor() + keystores.get(0).getScriptName() + scriptType.getCloseDescriptor()));
    }

    private static Policy homogeneousMulti(ScriptType scriptType, List<Keystore> keystores, Integer threshold) {
        StringBuilder builder = new StringBuilder();
        builder.append(scriptType.getDescriptor());
        builder.append(MULTISIG.getDescriptor());
        builder.append(threshold);
        for(Keystore keystore : keystores) {
            builder.append(",").append(keystore.getScriptName());
        }
        builder.append(MULTISIG.getCloseDescriptor());
        builder.append(scriptType.getCloseDescriptor());
        return new Policy(new Miniscript(builder.toString()));
    }

    public Policy copy() {
        Policy policy = new Policy(name, miniscript.copy());
        policy.setId(getId());
        return policy;
    }
}
