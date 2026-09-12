package com.sparrowwallet.drongo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WatchWalletTest {

    @Test
    public void electrumP2PKH() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("xpub661MyMwAqRbcFT5HwyRoP5hebbeRDvy2RGDTH2uxFyDPaf5FLtu4njuishddViQxTABZKzoWKuwpy6MsgfPvTw9pKnRGDL5eBxDej9kF54Z"));

        Assertions.assertEquals("FtQSq4PUsvZrFCh4eXuTDL94EaMNB1TGrk", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("17kCok3XAUHyL6kjzBF44e1YuzMmRXPuu5"), wallet.getReceivingAddress(1).toString());
        Assertions.assertEquals(ChainEncoding.address("1Dh3Lofy2cFdEQ2rk4Eq6fbPeQQ63pDdRN"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void iancolemanP2PKH() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("xpub6EEznxrqoN5HUXfD3QC3B8Vjw8Lj9UnRj17uTzNaBnEYN5xgwe6Un46Z443sSTBP2bzLZuDzygkdD1FtVWSexFmg4yAuCTxE2HxXFtz541z/*"));

        Assertions.assertEquals("FbKKomV6YSnXedF8kwAdrrUL6Zf87z1QmV", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("1GdWCzdt5oDYh5n1qeZQCxg5rQKVTuTMJg"), wallet.getReceivingAddress(1).toString());
    }

    @Test
    public void electrumP2WPKH() {
        WatchWallet wallet = new WatchWallet("", "zpub6njbcfTHEfK4U96Z8dBaTULdb1LGWMtj73yYZ76kfmE9nuf3KhNSsXfzDefz5KV6TreWjnQbgvnSmSttudzTugesV2HFunYu7gWYJUD4eoR");

        Assertions.assertEquals("fcn1q4s5v0u9qmmcp25mnr3mfzhyftjzw8mcceum57t", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1qffy90ge6wljh53t07q4al2pgsmuqgy48wrk8wq"), wallet.getReceivingAddress(1).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1q87fg9yjxratt4hemjn0m4re97n2p39ssq5xhv4"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void iancolemanP2SHP2WPKH() {
        WatchWallet wallet = new WatchWallet("", "ypub6Zken22QbjfomRUXki5v4ndP6T1DEtaBhGGZBvR4ocoooM44dFmnF8DyFmvcK76TKnuvdFfaPnicVvTAPdqEcbuEfKEqfnRoUjSkTB4u1os/*");

        Assertions.assertEquals("7VAKYVDYkHygnJjSk2HBdthJb3FZLLAcCj", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("3MgPnbF6UYM3FBhZWXoL2ebLPEa3zCCXLh"), wallet.getReceivingAddress(1).toString());
    }

    @Test
    public void bip84P2WPKH() {
        WatchWallet wallet = new WatchWallet("", "zpub6rFR7y4Q2AijBEqTUquhVz398htDFrtymD9xYYfG1m4wAcvPhXNfE3EfH1r1ADqtfSdVCToUG868RvUUkgDKf31mGDtKsAYz2oz2AGutZYs");

        Assertions.assertEquals("fcn1qcr8te4kr609gcawutmrza0j4xv80jy8zgw0x57", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1qnjg0jd8228aq7egyzacy8cys3knf9xvrerkf9g"), wallet.getReceivingAddress(1).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1q8c6fshw2dlwun7ekn9qwf37cu2rn755upcp6el"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void redditP2SHP2WPKH() {
        WatchWallet wallet = new WatchWallet("", "ypub6XiW9nhToS1gjVsFKzgmtWZuqo6V1YY7xaCns37aR3oYhFyAsTehAqV1iW2UCNtgWFQFkz3aNSZZbkfe5d1tD8MzjZuFJQn2XnczsxtjoXr");

        Assertions.assertEquals("7VAp1zE1j28mBDd8mQ7qBNhukg7tRQ22dM", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("35Jhf9LGCpb1ihJjWH7uLZ8othr1diuspS"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void electrumP2WSHMulti() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("wsh(multi(2,xpub699B7APGMoPLUrvPsXiBFrJRV8sTHDBHptpHSH36aESP5SLYs4VcEotnX1EvvA5ZoKF2rZ24Wh4U5ALxM21CfL5Kcj6Tu41PjRr2KKMkJTJ/0/*,xpub6Ds1jx5qxAtdczVBnJfHeGgpspzYuxnXHXLCoPZFFyyMoKJ7zzLgcERB1t7eDV1UuBQL1UKNxHFvcMJ7Zj6D2amdaA8gb21cZSXPrpG1bZr/0/*))"));

        Assertions.assertEquals("fcn1q2jxsrw70ug8jgskmhynvs49h3q5h8fglkdl3trvrc6wsde07wuzqlr600g", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1qzw9j02k6l7z598edcgjh5mks507xevhk34rmnerxv45ptsluf0pqyxmyve"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void electrumP2WSHMulti2() {
        WatchWallet wallet = new WatchWallet("", "wsh(multi(2,Zpub6yhnqjTYE82fc2U1UukQW6qEYsCcNoqsyPWPvL6Qi22YopXv8nD1a44zN87aUQcJr4YdE6DJKEA4xuBr5dzBQHZCBsbiUH7NAcFBgPyx3LB/0/*,Zpub74eXjdXzGCRsixpRAZT3U8ssQ15uhUa1dCFdRJvY3L2qo18He71qWUfpxfbL9e2EYuWKe1tH7qzgUSRVTAektLDVRKwCbAtyRW5j2yhqLiD/0/*))");

        Assertions.assertEquals("fcn1qa842ug2njv36ycnhq8wjcg6wxjv7p7h4v0tnl40u6nfxxxffyjnqrw6fwz", wallet.getReceivingAddress(0).toString());
        Assertions.assertEquals(ChainEncoding.address("bc1q3auk6c8f77dda0w8y9dz4yd3wqhkf4eufzk8x2quszvzzcyjk6rqgz70pd"), wallet.getChangeAddress(0).toString());
    }

    @Test
    public void electrumP2WSHMultiSingle() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("wsh(multi(2,xpub699B7APGMoPLUrvPsXiBFrJRV8sTHDBHptpHSH36aESP5SLYs4VcEotnX1EvvA5ZoKF2rZ24Wh4U5ALxM21CfL5Kcj6Tu41PjRr2KKMkJTJ/0/0,xpub6Ds1jx5qxAtdczVBnJfHeGgpspzYuxnXHXLCoPZFFyyMoKJ7zzLgcERB1t7eDV1UuBQL1UKNxHFvcMJ7Zj6D2amdaA8gb21cZSXPrpG1bZr/0/0))"));
        Assertions.assertEquals("fcn1q2jxsrw70ug8jgskmhynvs49h3q5h8fglkdl3trvrc6wsde07wuzqlr600g", wallet.getAddress(wallet.getOutputDescriptor().getChildDerivation()).toString());
    }

    @Test
    public void initialiseAddressesMultiple() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("wsh(multi(2,xpub699B7APGMoPLUrvPsXiBFrJRV8sTHDBHptpHSH36aESP5SLYs4VcEotnX1EvvA5ZoKF2rZ24Wh4U5ALxM21CfL5Kcj6Tu41PjRr2KKMkJTJ/0/*,xpub6Ds1jx5qxAtdczVBnJfHeGgpspzYuxnXHXLCoPZFFyyMoKJ7zzLgcERB1t7eDV1UuBQL1UKNxHFvcMJ7Zj6D2amdaA8gb21cZSXPrpG1bZr/0/*))"));
        wallet.initialiseAddresses();

        Assertions.assertTrue(wallet.containsAddress(wallet.getReceivingAddress(0)));
        Assertions.assertTrue(wallet.containsAddress(wallet.getChangeAddress(0)));
        Assertions.assertTrue(wallet.containsAddress(wallet.getReceivingAddress(500)));
    }

    @Test
    public void initialiseAddressesSingle() {
        WatchWallet wallet = new WatchWallet("", ChainEncoding.descriptor("wsh(multi(2,xpub699B7APGMoPLUrvPsXiBFrJRV8sTHDBHptpHSH36aESP5SLYs4VcEotnX1EvvA5ZoKF2rZ24Wh4U5ALxM21CfL5Kcj6Tu41PjRr2KKMkJTJ/0/0,xpub6Ds1jx5qxAtdczVBnJfHeGgpspzYuxnXHXLCoPZFFyyMoKJ7zzLgcERB1t7eDV1UuBQL1UKNxHFvcMJ7Zj6D2amdaA8gb21cZSXPrpG1bZr/0/0))"));
        wallet.initialiseAddresses();

        Assertions.assertTrue(wallet.containsAddress(wallet.getAddress(wallet.getOutputDescriptor().getChildDerivation())));
    }
}
