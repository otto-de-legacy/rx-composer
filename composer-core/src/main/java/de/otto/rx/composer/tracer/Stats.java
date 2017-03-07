package de.otto.rx.composer.tracer;

import org.slf4j.Logger;

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

public class Stats {

    private static final Logger LOG = getLogger(Stats.class);

    private final long startedTs;
    private final int numRequested;
    private final int numEmpty;
    private final int numErrors;
    private final int numNonEmpty;
    private final long avgNonEmptyMillis;
    private final long maxNonEmptyMillis;
    private final long runtime;
    private final String slowestFragment;

    Stats(final long startedTs, final int numRequested, final int numEmpty, final int numErrors,
          final int numNonEmpty, final long avgNonEmptyMillis, final long slowestNonEmptyMillis,
          final long runtime, final String slowestFragment) {
        this.startedTs = startedTs;
        this.numRequested = numRequested;
        this.numEmpty = numEmpty;
        this.numErrors = numErrors;
        this.numNonEmpty = numNonEmpty;
        this.avgNonEmptyMillis = avgNonEmptyMillis;
        this.maxNonEmptyMillis = slowestNonEmptyMillis;
        this.runtime = runtime;
        this.slowestFragment = slowestFragment;
    }

    public long getStartedTs() {
        return startedTs;
    }

    public int getNumRequested() {
        return numRequested;
    }

    public int getNumEmpty() {
        return numEmpty;
    }

    public int getNumErrors() {
        return numErrors;
    }

    public int getNumNonEmpty() {
        return numNonEmpty;
    }

    public long getAvgNonEmptyMillis() {
        return avgNonEmptyMillis;
    }

    public long getMaxNonEmptyMillis() {
        return maxNonEmptyMillis;
    }

    public long getRuntime() {
        return runtime;
    }

    public long getTotalRuntime() {
        return currentTimeMillis() - startedTs;
    }

    public String getSlowestFragment() {
        return slowestFragment;
    }

    public void logStats() {
        LOG.info(toString());
    }

    @Override
    public String toString() {
        return "Stats{" +
                "startedTs=" + startedTs +
                ", numRequested=" + numRequested +
                ", numEmpty=" + numEmpty +
                ", numErrors=" + numErrors +
                ", numNonEmpty=" + numNonEmpty +
                ", avgNonEmptyMillis=" + avgNonEmptyMillis +
                ", maxNonEmptyMillis=" + maxNonEmptyMillis +
                ", runtime=" + runtime +
                ", slowestFragment='" + slowestFragment + '\'' +
                '}';
    }

    public static StatsBuilder statsBuilder() {
        return new StatsBuilder();
    }

    public static Stats emptyStats() {
        return new StatsBuilder().build();
    }

    public static class StatsBuilder {
        long startedTs = currentTimeMillis();
        int numRequested = 0;
        int numEmpty = 0;
        int numErrors = 0;
        int numNonEmpty = 0;
        long avgNonEmptyMillis = 0;
        long slowestNonEmptyMillis = 0;
        long runtime = 0;
        String slowestFragment = "";

        private StatsBuilder() {
        }

        public Stats build() {
            return new Stats(startedTs, numRequested, numEmpty, numErrors, numNonEmpty, avgNonEmptyMillis, slowestNonEmptyMillis, runtime, slowestFragment);
        }
    }


}
