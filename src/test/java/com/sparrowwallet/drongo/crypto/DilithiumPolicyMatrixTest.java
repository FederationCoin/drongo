package com.sparrowwallet.drongo.crypto;

import com.sparrowwallet.drongo.protocol.KeyHashOrder;
import com.sparrowwallet.drongo.protocol.Transaction;
import com.sparrowwallet.drongo.protocol.TransactionWitness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DilithiumPolicyMatrixTest {
    @Test
    public void policyMatrixFortyFour() {
        assertMatrix(DilithiumKind.POLICY_44);
    }

    @Test
    public void policyMatrixEightySeven() {
        assertMatrix(DilithiumKind.POLICY_87);
    }

    @Test
    public void singlesHaveKindSizedWitness() {
        for(DilithiumKind kind : List.of(DilithiumKind.SINGLE_44, DilithiumKind.SINGLE_87)) {
            byte[] seed = new byte[32];
            Arrays.fill(seed, (byte)kind.param());
            DilithiumKind.Keypair key = kind.keygen(seed);
            Assertions.assertEquals(kind.pubkeySize(), key.pubkey().length);
            Assertions.assertEquals(32, kind.keyHash(key.pubkey()).length);
            TransactionWitness dummy = kind.dummyWitness(new Transaction(), 1, 1);
            Assertions.assertEquals(2, dummy.getPushes().size());
            Assertions.assertEquals(kind.pubkeySize(), dummy.getPushes().get(0).length);
            Assertions.assertEquals(kind.signatureSize(), dummy.getPushes().get(1).length);
            Assertions.assertThrows(UnsupportedOperationException.class, () -> kind.policyProgram(1, hashes(1)));
        }
    }

    @Test
    public void kindsDoNotShareAKeyHash() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte)0x44);
        byte[] h44 = DilithiumKind.SINGLE_44.keyHash(DilithiumKind.SINGLE_44.keygen(seed).pubkey());
        byte[] h87 = DilithiumKind.SINGLE_87.keyHash(DilithiumKind.SINGLE_87.keygen(seed).pubkey());
        Assertions.assertFalse(Arrays.equals(h44, h87));
    }

    private static void assertMatrix(DilithiumKind kind) {
        int[][] cases = {{1, 1}, {1, 2}, {2, 3}, {3, 5}, {11, 12}, {12, 12}};
        for(int[] pair : cases) {
            int threshold = pair[0];
            int n = pair[1];
            List<byte[]> hashes = hashes(n);
            byte[] program = kind.policyProgram(threshold, hashes);
            Assertions.assertTrue(program.length > 0, threshold + "-of-" + n);
            List<byte[]> reversed = new ArrayList<>(hashes);
            java.util.Collections.reverse(reversed);
            Assertions.assertArrayEquals(program, kind.policyProgram(threshold, reversed));
            TransactionWitness dummy = kind.dummyWitness(new Transaction(), n, threshold);
            Assertions.assertEquals(n, dummy.getPushes().size());
            int signed = 0;
            int skipped = 0;
            int item = kind.pubkeySize() + kind.signatureSize();
            for(byte[] push : dummy.getPushes()) {
                if(push.length == item) {
                    signed++;
                } else {
                    skipped++;
                    Assertions.assertEquals(0, push.length);
                }
            }
            Assertions.assertEquals(threshold, signed);
            Assertions.assertEquals(n - threshold, skipped);
        }
        List<byte[]> two = hashes(2);
        List<byte[]> extra = new ArrayList<>(two);
        extra.add(hashAt(3));
        Assertions.assertNotEquals(
                kind.policyProgram(1, two).length,
                kind.policyProgram(1, extra).length);
        Assertions.assertThrows(IllegalArgumentException.class, () -> kind.policyProgram(1, List.of(hashAt(1), hashAt(1))));
        byte[] highFirst = hashAt(0xff);
        byte[] lowSecond = hashAt(0x01);
        List<byte[]> hexOrderTrap = List.of(highFirst, lowSecond);
        byte[] sorted = kind.policyProgram(1, hexOrderTrap);
        List<byte[]> memcmp = new ArrayList<>(hexOrderTrap);
        memcmp.sort(KeyHashOrder.INSTANCE);
        Assertions.assertArrayEquals(sorted, kind.policyProgram(1, memcmp));
    }

    private static List<byte[]> hashes(int n) {
        List<byte[]> hashes = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            hashes.add(hashAt(i + 1));
        }
        return hashes;
    }

    private static byte[] hashAt(int first) {
        byte[] hash = new byte[32];
        hash[0] = (byte)first;
        hash[31] = (byte)(first ^ 0x5a);
        return hash;
    }
}
