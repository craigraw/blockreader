# BlockReader

BlockReader is a small command line application to benchmark block reading performance.
Currently, it is using `bitcoin-cli` to read blocks using the `getblockheader` followed by the `getblock` call to retrieve the block hex for a configurable number of blocks starting at a random block height around 500,000, using a configurable number of threads.

### Usage

```shell
git clone git@github.com:craigraw/blockreader.git
cd blockreader
./gradlew run --args "<rpcaddress> <rpcuser> <rpcpassword> [numBlocks] [numThreads]"
```

Results are printed to stdout.