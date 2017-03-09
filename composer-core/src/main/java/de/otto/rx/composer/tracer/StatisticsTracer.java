package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Statistics;
import de.otto.rx.composer.content.Statistics.StatsBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static de.otto.rx.composer.content.Statistics.statsBuilder;
import static de.otto.rx.composer.tracer.EventType.*;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Traces and gathers statistics for a single request execution.
 */
public final class StatisticsTracer implements Tracer {

    private static final Logger LOG = getLogger(StatisticsTracer.class);

    private final ConcurrentLinkedQueue<TraceEvent> events = new ConcurrentLinkedQueue<>();
    private final long startedTs = currentTimeMillis();

    private StatisticsTracer() {
    }

    public static StatisticsTracer statisticsTracer() {
        return new StatisticsTracer();
    }

    @Override
    public void trace(final TraceEvent event) {
        events.add(event);
    }

    @Override
    public ImmutableList<TraceEvent> getEvents() {
        return copyOf(events);
    }

    @Override
    public Statistics getStatistics() {
        StatsBuilder stats = statsBuilder();
        stats.startedTs = startedTs;
        for (final TraceEvent event : events) {
            switch (event.getType()) {
                case STARTED:
                    ++stats.numRequested;
                    break;
                case FALLBACK_STARTED:
                    ++stats.numFallbacksRequested;
                    break;
                case FALLBACK_COMPLETED:
                case COMPLETED:
                    gatherCompletedStatistics(stats, event);
                    break;
                case ERROR:
                    ++stats.numErrors;
                    break;
                default:
                    break;
            }
        }
        stats.runtime = currentTimeMillis() - startedTs;
        return stats.build();
    }

    private void gatherCompletedStatistics(final StatsBuilder stats, final TraceEvent event) {
        if (event.isNonEmptyContent()) {
            if (event.getType().equals(COMPLETED)) {
                ++stats.numNonEmpty;
            } else {
                ++stats.numNonEmptyFallbacks;
            }

            long fragmentRuntime = event.getTimestamp() - startedTs;
            stats.sumNonEmptyMillis += fragmentRuntime;
            if (fragmentRuntime > stats.slowestNonEmptyMillis) {
                stats.slowestFragment = event.getPosition().name();
                stats.slowestNonEmptyMillis = fragmentRuntime;
            }
        } else {
            ++stats.numEmpty;
            long fragmentRuntime = event.getTimestamp() - startedTs;
            if (fragmentRuntime > stats.slowestNonEmptyMillis) {
                stats.slowestFragment = event.getPosition().name();
            }
        }
    }
}
