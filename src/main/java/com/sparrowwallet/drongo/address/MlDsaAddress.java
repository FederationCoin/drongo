package com.sparrowwallet.drongo.address;

import com.sparrowwallet.drongo.Network;
import com.sparrowwallet.drongo.protocol.Bech32;
import com.sparrowwallet.drongo.protocol.ModernSig;
import com.sparrowwallet.drongo.protocol.Script;

public abstract class MlDsaAddress extends Address {
    protected MlDsaAddress(byte[] program) {
        super(program);
        if(program.length == 32) {
            return;
        }
        Script script = new Script(program);
        if(!ModernSig.isTemplate(script) || script.getProgram().length > ModernSig.POLICY_SCRIPT_MAX) {
            throw new IllegalArgumentException("ML-DSA address is a modern-sig locking script");
        }
    }

    @Override
    public int getVersion(Network network) {
        return 0;
    }

    @Override
    public String getAddress(Network network) {
        byte[] script = getOutputScript().getProgram();
        byte[] five = Bech32.convertBits(script, 0, script.length, 8, 5, true);
        return Bech32.encode(network.getBech32AddressHRP(), Bech32.Encoding.BECH32, five);
    }
}
