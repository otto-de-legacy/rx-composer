package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Statistics;
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

    public void trace(final TraceEvent event) {
        events.add(event);
        switch (event.getType()) {
            case COMPLETED:
                LOG.trace("{} fetching {} content for position {} from {}", event.getType(), event.isNonEmptyContent() ? "AVAILABLE" : "UNAVAILABLE", event.getPosition().name(), event.getSource());
                break;
            case ERROR:
                if (event.getSource().isEmpty()) {
                    LOG.error("Error fetching content for position: {}", event.getPosition().name());
                } else {
                    LOG.error("Error fetching content {} for position {}: {}", event.getSource(), event.getPosition().name(), event.getErrorMessage());
                }
                break;
            case STARTED:
                LOG.trace("{} fetching content for position {} from {}", event.getType(), event.getPosition().name(), event.getSource());
                break;
            default:
                throw new IllegalStateException("Unknown EventType " + event.getType());
        }
    }

    public ImmutableList<TraceEvent> getEvents() {
        return copyOf(events);
    }

    public Statistics gatherStatistics() {
        Statistics.StatsBuilder stats = statsBuilder();
        stats.startedTs = startedTs;

        long sumNonEmptyMillis = 0;

        for (final TraceEvent event : events) {
            switch (event.getType()) {
                case STARTED:
                    ++stats.numRequested;
                    break;
                case COMPLETED:
                    if (event.isNonEmptyContent()) {
                        ++stats.numNonEmpty;
                        long fragmentRuntime = event.getTimestamp() - startedTs;
                        sumNonEmptyMillis += fragmentRuntime;
                        if (fragmentRuntime > stats.slowestNonEmptyMillis) {
                            stats.slowestFragment = event.getPosition().name();
                            stats.slowestNonEmptyMillis = fragmentRuntime;
                        }
                        stats.avgNonEmptyMillis = sumNonEmptyMillis / stats.numNonEmpty;
                    } else {
                        ++stats.numEmpty;
                    }
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
}
