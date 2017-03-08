package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Statistics;
import de.otto.rx.composer.content.Statistics.StatsBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static de.otto.rx.composer.content.Statistics.statsBuilder;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Traces and gathers statistics for a single request execution.
 */
public final class Tracer {

    private static final Logger LOG = getLogger(Tracer.class);

    private final ConcurrentLinkedQueue<TraceEvent> events = new ConcurrentLinkedQueue<>();
    private final long startedTs = currentTimeMillis();

    public Tracer() {
        LOG.trace("STARTED fetching page");
    }
    public void trace(final TraceEvent event) {
        events.add(event);
        switch (event.getType()) {
            case COMPLETED:
                LOG.trace("COMPLETED fetching {} content for position {} from {}", event.isNonEmptyContent() ? "AVAILABLE" : "UNAVAILABLE", event.getPosition().name(), event.getSource());
                break;
            case FALLBACK_COMPLETED:
                LOG.trace("COMPLETED fetching {} FALLBACK content for position {} from {}", event.isNonEmptyContent() ? "AVAILABLE" : "UNAVAILABLE", event.getPosition().name(), event.getSource());
                break;
            case ERROR:
                if (event.getSource().isEmpty()) {
                    LOG.error("ERROR fetching content for position: {}", event.getPosition().name());
                } else {
                    LOG.error("ERROR fetching content {} for position {}: {}", event.getSource(), event.getPosition().name(), event.getErrorMessage());
                }
                break;
            case STARTED:
                LOG.trace("STARTED fetching content for position {} from {}", event.getPosition().name(), event.getSource());
                break;
            case FALLBACK_STARTED:
                LOG.info("STARTED fetching FALLBACK content for position {} from {}", event.getPosition().name(), event.getSource());
                break;
            default:
                throw new IllegalStateException("Unknown EventType " + event.getType());
        }
    }

    public ImmutableList<TraceEvent> getEvents() {
        return copyOf(events);
    }

    public Statistics gatherStatistics() {
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
            if (event.getType().equals(EventType.COMPLETED)) {
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
