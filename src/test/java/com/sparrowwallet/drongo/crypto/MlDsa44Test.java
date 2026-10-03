package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.Utils;
import com.sparrowwallet.drongo.wallet.StallPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MlDsa44Test {
    @Test
    public void keygenMatchesNodeTool() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x11);
        MlDsa44.Keypair key = MlDsa44.keygen(seed);
        Assertions.assertEquals(1312, key.pubkey().length);
        Assertions.assertEquals(2560, key.secret().length);
        Assertions.assertEquals("54c981c4059222f4f9b4a406a13230cd2786ab8a3e026d5700bedeb2b3b64c82",
                Utils.bytesToHex(MlDsa44.keyHash(key.pubkey())));
        byte[] message = new byte[32];
        Arrays.fill(message, (byte)0xaa);
        byte[] signature = MlDsa44.sign(key.secret(), message);
        Assertions.assertEquals(2420, signature.length);
        Assertions.assertEquals("5793ddede5ba766ada8ff8366b720ddf", Utils.bytesToHex(Arrays.copyOf(signature, 16)));
        Assertions.assertTrue(MlDsa44.verify(key.pubkey(), signature, message));
    }

    @Test
    public void stallWaitsTwelve() {
        Assertions.assertEquals(6, StallPolicy.requiredConfirmations(StallPolicy.STALL_TIP_MS, 6));
        Assertions.assertEquals(12, StallPolicy.requiredConfirmations(StallPolicy.STALL_TIP_MS + 1, 6));
    }
}
