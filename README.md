# Drongo

Java library for FederationCoin addresses, transactions, PSBTs, and related identity. Federation Sparrow builds it as a git submodule. You do not install or run this tree by itself.

Not Bitcoin. Not affiliated with Sparrow Wallet. Experimental. No warranty; see the [Apache 2.0 license](LICENSE).

## For users

Use [Federation Sparrow](https://github.com/FederationCoin/federation-sparrow) on **testnet** (`tfcn`, node P2P 35333 / RPC 35332). Dummy MAIN is not launched. Chain identity in the wallet (magic, HRP, BIP32 print form, parked Taproot) lives here, not in the Sparrow Java UI tree.

Bitcoin `tpub` / `xpub` strings will not import. Recreate a testnet wallet after upgrading.

## For developers

Forked from [privkeyio/drongo](https://github.com/privkeyio/drongo) (itself from sparrowwallet; inspired in part by [bitcoinj](https://bitcoinj.org)). Origin is `git@github.com:FederationCoin/drongo.git`. Mainline is `federationcoin`. GitHub is detached from that fork; **never push** privkeyio or sparrowwallet.

Java package names remain `com.sparrowwallet.drongo` (upstream layout). Do not fetch `code.sparrowwallet.com` or Maven Central `com.sparrowwallet` artifacts for this fork. `maven.federationcoin.org` is not provisioned.

### Clone and build

```bash
git clone git@github.com:FederationCoin/drongo.git
git checkout federationcoin
./gradlew jar
./gradlew test
```

Sparrow consumes this as a submodule (`include 'drongo'`), not a published jar.

### Branching

Work on a branch off `federationcoin`. Open a same-repo pull request; a human merges. Do not push straight to mainline. This library has no `get-to-mainnet` branch this iteration.

### Release

Independent Gradle versions and git tags come later. Until then, Sparrow pins a gitlink SHA that must already be on origin `federationcoin` (Package checkout fails with `not our ref` otherwise). No Maven publish, no GitHub Packages.

### Quality

Code quality checks and metrics will be added over time.
