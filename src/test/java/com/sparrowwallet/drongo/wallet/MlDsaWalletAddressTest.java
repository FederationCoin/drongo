package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.MlDsaAddress;
import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.policy.Policy;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.protocol.ScriptType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MlDsaWalletAddressTest {
    @Test
    public void receiveZeroMatchesChildKeyHash() throws Exception {
        byte[] entropy = new byte[16];
        Arrays.fill(entropy, (byte)0x21);
        DeterministicSeed seed = new DeterministicSeed(entropy, "", 0L);
        Keystore keystore = new Keystore();
        keystore.setSeed(seed);
        keystore.setSource(KeystoreSource.SW_SEED);
        Wallet wallet = new Wallet("ml");
        wallet.setPolicyType(PolicyType.SINGLE_HD);
        wallet.setScriptType(ScriptType.MLDSA_SINGLE);
        wallet.getKeystores().add(keystore);
        wallet.setDefaultPolicy(Policy.getPolicy(wallet.getPolicyType(), wallet.getScriptType(), wallet.getKeystores(), 1));

        WalletNode receive0 = new WalletNode(wallet, KeyPurpose.RECEIVE, 0);
        Address address = wallet.getAddress(receive0);
        Assertions.assertInstanceOf(MlDsaAddress.class, address);
        byte[] expected = MlDsa44.keyHash(MlDsa44.keygen(MlDsa44.childSeed(seed.getSeedBytes(), 0)).pubkey());
        Assertions.assertArrayEquals(expected, address.getData());

        WalletNode change0 = new WalletNode(wallet, KeyPurpose.CHANGE, 0);
        Address change = wallet.getAddress(change0);
        byte[] changeExpected = MlDsa44.keyHash(MlDsa44.keygen(MlDsa44.childSeed(seed.getSeedBytes(), Wallet.mlDsaChildIndex(change0))).pubkey());
        Assertions.assertArrayEquals(changeExpected, change.getData());
        Assertions.assertFalse(Arrays.equals(address.getData(), change.getData()));
    }
}
