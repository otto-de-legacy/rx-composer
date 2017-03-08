package de.otto.rx.composer.tracer;

/**
 * Event types used by the {@link Tracer} to distinguish different kind of events.
 * <p>
 *     Depending on the event type, statistics are gathered that can be used for debugging and performance analysis.
 * </p>
 */
public enum EventType {
    STARTED, COMPLETED, FALLBACK_STARTED, FALLBACK_COMPLETED, ERROR
}
