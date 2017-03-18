package de.schmaun.ourrecipes.Utils;

public class StopWatch {
    private long start = 0;
    private long end = 0;
    private long duration;

    public static StopWatch createAndStart() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        return stopWatch;
    }

    public void start() {
        end = 0;
        start = System.currentTimeMillis();
    }

    public long stop() {
        end = System.currentTimeMillis();
        duration = end - start;

        return duration;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDuration() {
        return duration;
    }
}
