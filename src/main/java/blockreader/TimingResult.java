package blockreader;

public class TimingResult {
    private final long bytesRead;
    private final long elapsed;

    public TimingResult(long bytesRead, long elapsed) {
        this.bytesRead = bytesRead;
        this.elapsed = elapsed;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public long getElapsed() {
        return elapsed;
    }
}
