package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Statistics;
import de.otto.rx.composer.page.Page;

import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.content.Statistics.emptyStats;

/**
 * {@inheritDoc}
 * <p>
 *     This implementation is doing nothing.
 * </p>
 */
public final class NoOpTracer implements Tracer {

    private static final ImmutableList<TraceEvent> EMPTY_LIST = of();
    private static final Statistics EMPTY_STATS = emptyStats();
    private static final NoOpTracer NO_OP_TRACER = new NoOpTracer();

    private NoOpTracer() {
    }

    public static Tracer noOpTracer() {
        return NO_OP_TRACER;
    }

    /**
     * Processes a single {@link TraceEvent}.
     *
     * @param event the traced event.
     */
    @Override
    public void trace(final TraceEvent event) {
    }

    /**
     * Returns the traced events, or an empty list, of the Tracer implementation does not gather events.
     *
     * @return immutable list of traced events.
     */
    @Override
    public ImmutableList<TraceEvent> getEvents() {
        return EMPTY_LIST;
    }

    /**
     * Returns the statistics gathered while fetching a {@link Page}.
     *
     * @return execution statistics.
     */
    @Override
    public Statistics getStatistics() {
        return EMPTY_STATS;
    }
}
