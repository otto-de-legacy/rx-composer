package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Traces and gathers statistics for a single request execution.
 */
public final class Tracer {

    private static final Logger LOG = getLogger(Tracer.class);

    private final ConcurrentLinkedQueue<TraceEvent> events = new ConcurrentLinkedQueue<>();
    private final long startedTimestamp = currentTimeMillis();

    public void trace(final TraceEvent event) {
        events.add(event);
        switch (event.getType()) {
            case COMPLETED:
                LOG.trace("{} fetching {} content for position {} from {}", event.getType(), event.isNonEmptyContent() ? "available" : "unavailable", event.getPosition().name(), event.getSource());
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

    public void gatherStatistics() {
        int numRequested = 0;
        int numEmpty = 0;
        int numErrors = 0;
        int numNonEmpty = 0;
        long sumNonEmptyMillis = 0;
        long avgNonEmptyMillis = 0;
        long slowestNonEmptyMillis = 0;
        long runtime = 0;
        String slowestFragment = "";
        for (final TraceEvent event : events) {
            switch (event.getType()) {
                case STARTED:
                    ++numRequested;
                    break;
                case COMPLETED:
                    if (event.isNonEmptyContent()) {
                        ++numNonEmpty;
                        long fragmentRuntime = event.getTimestamp() - startedTimestamp;
                        sumNonEmptyMillis += fragmentRuntime;
                        if (fragmentRuntime > slowestNonEmptyMillis) {
                            slowestFragment = event.getPosition().name() + " from " + event.getSource();
                            slowestNonEmptyMillis = fragmentRuntime;
                        }
                        avgNonEmptyMillis = sumNonEmptyMillis / numNonEmpty;
                    } else {
                        ++numEmpty;
                    }
                    break;
                case ERROR:
                    ++numErrors;
                    break;
                default:
                    break;
            }
        }
        runtime = currentTimeMillis() - startedTimestamp;
        LOG.info("Request Stats: requested: {}, with content: {}, empty: {}, errors: {}, slowest: {} ({}ms), avg: {}ms, total runtime: {}ms", numRequested, numNonEmpty, numEmpty, numErrors, slowestFragment, slowestNonEmptyMillis, avgNonEmptyMillis, runtime);
    }
}
