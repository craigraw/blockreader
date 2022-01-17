package blockreader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class BlockReader {
    private static final int BASE_HEIGHT = 514000;
    private static final int DEFAULT_NUM_BLOCKS = 10;
    private static final int DEFAULT_NUM_THREADS = 4;

    private final String ipAddress;
    private final String user;
    private final String password;
    private final int numBlocks;
    private final int numThreads;

    private final ExecutorService executorService;

    private final List<Future<TimingResult>> timingResultFutures = new ArrayList<>();

    public BlockReader(String ipAddress, String user, String password, int numBlocks, int numThreads) {
        this.ipAddress = ipAddress;
        this.user = user;
        this.password = password;
        this.numBlocks = numBlocks;
        this.numThreads = numThreads;
        this.executorService = Executors.newFixedThreadPool(numThreads);
    }

    public void start() throws Exception {
        for(int i = 0; i < numThreads; i++) {
            RPCReader rpcReader = new RPCReader(this, BASE_HEIGHT + (int)(Math.random() * 10000), numBlocks);
            Future<TimingResult> timingResultFuture = executorService.submit(rpcReader);
            timingResultFutures.add(timingResultFuture);
        }

        while(timingResultFutures.stream().anyMatch(timingResultFuture -> !timingResultFuture.isDone())) {
            Thread.sleep(300);
        }

        List<TimingResult> timingResults = timingResultFutures.stream().map(timingResultFuture -> {
            try {
                return timingResultFuture.get();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        long elapsed = timingResults.stream().mapToLong(TimingResult::getElapsed).max().orElse(0);
        long totalBytes = timingResults.stream().mapToLong(TimingResult::getBytesRead).sum();

        double rate = (double)totalBytes * 1000 / elapsed;
        double kBRate = rate / 1000;
        System.out.println("Total " + totalBytes + " bytes in " + elapsed + " ms (" + String.format("%.2f", kBRate) + " Kb/sec)");

        executorService.shutdown();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public static void main(String[] args) throws Exception {
        if(args.length < 3) {
            System.out.println("Usage: java -jar blockreader.jar <rpcaddress> <rpcuser> <rpcpassword> [numBlocks] [numThreads]");
            System.exit(1);
        }

        BlockReader blockReader = new BlockReader(args[0], args[1], args[2],
                args.length > 3 ? Integer.parseInt(args[3]) : DEFAULT_NUM_BLOCKS,
                args.length > 4 ? Integer.parseInt(args[4]) : DEFAULT_NUM_THREADS);
        blockReader.start();
    }
}
