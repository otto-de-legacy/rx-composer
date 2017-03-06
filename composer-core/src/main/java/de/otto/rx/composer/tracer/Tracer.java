package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.ErrorContent;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.ImmutableList.copyOf;
import static de.otto.rx.composer.tracer.EventType.COMPLETED;
import static de.otto.rx.composer.tracer.EventType.ERROR;
import static de.otto.rx.composer.tracer.EventType.STARTED;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Traces and gathers statistics for a single request execution.
 */
public final class Tracer {

    private static final Logger LOG = getLogger(Tracer.class);

    private final ConcurrentLinkedQueue<TraceEvent> events = new ConcurrentLinkedQueue<>();
    private final long startedTimestamp = currentTimeMillis();

    public void traceFragmentStarted(final Position position, final String url) {
        events.add(new TraceEvent(STARTED, position, url, false));
        LOG.trace("Start fetching content for position {} from {}", position.name(), url);
    }

    public void traceFragmentCompleted(final Position position, final String url, final boolean available) {
        events.add(new TraceEvent(COMPLETED, position, url, available));
        LOG.trace("Got next {} content for position {} from {} with status {}", available ? "available" : "unavailable", position.name(), url);
    }

    public void traceError(final ErrorContent errorContent) {
        events.add(new TraceEvent(ERROR, errorContent.getPosition(), errorContent.getSource(), false));
        LOG.error("Created ErrorContent for position " + errorContent.getPosition() + ": " + errorContent.getThrowable().getMessage());
    }

    public void traceError(final Position position, final String url, final Throwable t) {
        events.add(new TraceEvent(ERROR, position, url, false));
        LOG.error("Error fetching content {} for position {}: {}", url, position.name(), t.getMessage());
    }

    public void traceCircuitBreakerException(final Position position, final String url, final Throwable t) {
        events.add(new TraceEvent(ERROR, position, url, false));
        LOG.error("Caught Exception from CircuitBreaker: for position {}: {} ({})", position.name(), t.getCause().getMessage(), t.getMessage());
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
        LOG.info("Request Stats: requested: {} with content: {} empty: {} errors: {} slowest: {} ({}ms) avg: {}ms total runtime: {}ms", numRequested, numNonEmpty, numEmpty, numErrors, slowestFragment, slowestNonEmptyMillis, avgNonEmptyMillis, runtime);
    }
}
