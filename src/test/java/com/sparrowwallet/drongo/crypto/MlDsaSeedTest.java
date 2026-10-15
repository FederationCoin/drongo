package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class MlDsaSeedTest {
    @Test
    public void pinnedVectorMatchesNode() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x42);
        byte[] out = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, 7);
        Assertions.assertEquals("8d3755b302d8a066e6ae846b00e329cfd597043e812dc509b240876c171e8f68", Utils.bytesToHex(out));
    }

    @Test
    public void kindAndIndexDiverge() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        byte[] a = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_44, 0);
        byte[] b = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, 0);
        byte[] c = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_44, 1);
        Assertions.assertFalse(Arrays.equals(a, b));
        Assertions.assertFalse(Arrays.equals(a, c));
        Assertions.assertFalse(Arrays.equals(b, c));
    }

    @Test
    public void emptySeedFails() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> MlDsaSeed.derive(new byte[0], MlDsaSeed.Kind.DILITHIUM_44, 0));
    }

    @Test
    public void changeBitDiverges() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        byte[] recv = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, MlDsaSeed.kdfIndex(0, false));
        byte[] change = MlDsaSeed.derive(seed, MlDsaSeed.Kind.DILITHIUM_87, MlDsaSeed.kdfIndex(0, true));
        Assertions.assertEquals(0, MlDsaSeed.kdfIndex(0, false));
        Assertions.assertEquals(Integer.MIN_VALUE, MlDsaSeed.kdfIndex(0, true));
        Assertions.assertFalse(Arrays.equals(recv, change));
    }

    @Test
    public void policyXprvsMatchNodeScriptPubKey() {
        byte[] xprv0 = new byte[32];
        Arrays.fill(xprv0, (byte)0xa1);
        byte[] xprv1 = new byte[32];
        Arrays.fill(xprv1, (byte)0xb2);
        byte[] d0 = MlDsaSeed.derive(xprv0, MlDsaSeed.Kind.POLICY_44, MlDsaSeed.kdfIndex(MlDsaSeed.policyIndex(0, 0), false));
        byte[] d1 = MlDsaSeed.derive(xprv1, MlDsaSeed.Kind.POLICY_44, MlDsaSeed.kdfIndex(MlDsaSeed.policyIndex(0, 1), false));
        MlDsa44.Keypair k0 = MlDsa44.keygen(d0);
        MlDsa44.Keypair k1 = MlDsa44.keygen(d1);
        byte[] script = MlDsa44.policyProgram(1, List.of(MlDsa44.keyHash(k0.pubkey()), MlDsa44.keyHash(k1.pubkey())));
        Assertions.assertEquals("51202b84379230309a0492bcb24fd24d52b1b81f89cfc4f821890770fd0e7cf335d920bf4fc5ead1b5876b86d9083d563608f8446e6f2b8288be542c2c99e213b66017525151bc", Utils.bytesToHex(script));
    }
}
