package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.ExtendedKey;
import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.address.Address;
import com.sparrowwallet.drongo.address.MlDsaAddress;
import com.sparrowwallet.drongo.crypto.ChildNumber;
import com.sparrowwallet.drongo.crypto.DeterministicKey;
import com.sparrowwallet.drongo.crypto.HDKeyDerivation;
import com.sparrowwallet.drongo.crypto.MlDsa44;
import com.sparrowwallet.drongo.policy.Policy;
import com.sparrowwallet.drongo.policy.PolicyType;
import com.sparrowwallet.drongo.protocol.ScriptType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MlDsaWalletAddressTest {
    @Test
    public void receiveZeroMatchesBip32ChildKeyHash() throws Exception {
        byte[] entropy = new byte[16];
        Arrays.fill(entropy, (byte)0x21);
        DeterministicSeed seed = new DeterministicSeed(entropy, "", 0L);
        Keystore keystore = Keystore.fromSeed(seed, PolicyType.SINGLE_MLDSA44, ScriptType.MLDSA_SINGLE.getDefaultDerivation());
        Wallet wallet = new Wallet("ml");
        wallet.setPolicyType(PolicyType.SINGLE_MLDSA44);
        wallet.setScriptType(ScriptType.MLDSA_SINGLE);
        wallet.getKeystores().clear();
        wallet.getKeystores().add(keystore);
        wallet.setDefaultPolicy(Policy.getPolicy(wallet.getPolicyType(), wallet.getScriptType(), wallet.getKeystores(), 1));

        WalletNode receive0 = new WalletNode(wallet, KeyPurpose.RECEIVE, 0);
        Address address = wallet.getAddress(receive0);
        Assertions.assertInstanceOf(MlDsaAddress.class, address);
        Assertions.assertArrayEquals(programAt(seed, ScriptType.MLDSA_SINGLE.getDefaultDerivation(), KeyPurpose.RECEIVE, 0),
                address.getData());

        WalletNode change0 = new WalletNode(wallet, KeyPurpose.CHANGE, 0);
        Address change = wallet.getAddress(change0);
        Assertions.assertArrayEquals(programAt(seed, ScriptType.MLDSA_SINGLE.getDefaultDerivation(), KeyPurpose.CHANGE, 0),
                change.getData());
        Assertions.assertFalse(Arrays.equals(address.getData(), change.getData()));
    }

    private static byte[] programAt(DeterministicSeed seed, List<ChildNumber> account, KeyPurpose purpose, int index) throws Exception {
        List<ChildNumber> path = new ArrayList<>(account);
        path.add(purpose.getPathIndex());
        path.add(new ChildNumber(index));
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        byte[] child = new ExtendedKey(master, new byte[4], ChildNumber.ZERO).getKey(path).getPrivKeyBytes();
        return MlDsa44.keyHash(MlDsa44.keygen(child).pubkey());
    }
}
