package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.ExtendedKey;
import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Bip32ChildIsMlDsaSeedTest {
    @Test
    public void knotsHdPathMatchesNodeVector() {
        byte[] masterSeed = new byte[32];
        masterSeed[0] = 0x0b;
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(masterSeed);
        ExtendedKey xprv = new ExtendedKey(master, new byte[4], ChildNumber.ZERO);
        DeterministicKey child = xprv.getKey(List.of(
                ChildNumber.ZERO_HARDENED, ChildNumber.ZERO_HARDENED, ChildNumber.ZERO_HARDENED));
        byte[] seed = child.getPrivKeyBytes();
        Assertions.assertEquals("e1d174161b4abbe62bc2f998c79e8cf32d4b03f2d6f412b722645ad052c3061d",
                Utils.bytesToHex(seed));
        Assertions.assertEquals("295be60dbfc76f27b3184cce5c4754737276a6835137ef8b6849859d063a37d4",
                Utils.bytesToHex(MlDsa44.keyHash(MlDsa44.keygen(seed).pubkey())));
        Assertions.assertEquals("f0a75c325805efe2a9267c5a626302401273cbbbe7dac033187abf55106e763c",
                Utils.bytesToHex(MlDsa87.keyHash(MlDsa87.keygen(seed).pubkey())));
    }
}
