package blockreader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RPCReader implements Callable<TimingResult> {
    private final BlockReader reader;
    private final int startHeight;
    private final int endHeight;

    private long charsRead = 0;

    public RPCReader(BlockReader reader, int startHeight, int total) {
        this.reader = reader;
        this.startHeight = startHeight;
        this.endHeight = startHeight + total;
    }

    @Override
    public TimingResult call() {
        long start = System.currentTimeMillis();
        for(int blockHeight = startHeight; blockHeight < endHeight; blockHeight++) {
            String blockHash = getBlockHash(blockHeight);
            if(blockHash != null) {
                String blockHex = getBlockHex(blockHash);
                if(blockHex != null) {
                    charsRead += blockHex.length();
                }
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        double rate = (double)charsRead * 1000 / elapsed;
        double kBRate = rate / 1000;
        System.out.println("Read " + charsRead + " bytes in " + elapsed + " ms (" + String.format("%.2f", kBRate) + " Kb/sec)");

        return new TimingResult(charsRead, elapsed);
    }

    private String getBlockHash(int blockHeight) {
        List<String> command = getBaseCommand();
        command.add("getblockhash");
        command.add(Integer.toString(blockHeight));

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getBlockHex(String blockHash) {
        List<String> command = getBaseCommand();
        command.add("getblock");
        command.add(blockHash);
        command.add("0");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<String> getBaseCommand() {
        return new ArrayList<>(List.of("bitcoin-cli", "-rpcconnect=" + reader.getIpAddress(), "-rpcport=8332", "-rpcuser=" + reader.getUser(), "-rpcpassword=" + reader.getPassword()));
    }
}
