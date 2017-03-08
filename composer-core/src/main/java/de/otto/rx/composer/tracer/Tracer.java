package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Statistics;
import de.otto.rx.composer.page.Page;

/**
 * Traces and gathers information about {@link de.otto.rx.composer.page.Page#fetchWith(Parameters) fetching}
 * a single {@link Page}.
 */
public interface Tracer {

    /**
     * Processes a single {@link TraceEvent}.
     *
     * @param event the traced event.
     */
    void trace(final TraceEvent event);

    /**
     * Returns the traced events, or an empty list, of the Tracer implementation does not gather events.
     *
     * @return immutable list of traced events.
     */
    ImmutableList<TraceEvent> getEvents();

    /**
     * Returns the statistics gathered while fetching a {@link Page}.
     *
     * @return execution statistics.
     */
    Statistics getStatistics();

}
