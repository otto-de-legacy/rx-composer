package de.otto.rx.composer.tracer;

import de.otto.rx.composer.content.Position;

import static de.otto.rx.composer.tracer.EventType.*;
import static java.lang.System.currentTimeMillis;

/**
 * Created by guido on 06.03.17.
 */
public final class TraceEvent {

    private final EventType type;
    private final long timestamp = currentTimeMillis();
    private final Position position;
    private final String source;
    private final boolean nonEmptyContent;
    private final String errorMessage;

    private TraceEvent(final EventType type, final Position position, final String source, final boolean nonEmptyContent, final String errorMessage) {
        this.type = type;
        this.position = position;
        this.source = source;
        this.nonEmptyContent = nonEmptyContent;
        this.errorMessage = errorMessage;
    }

    public static TraceEvent fragmentStarted(final Position position, final String source) {
        return new TraceEvent(STARTED, position, source, false, "");
    }

    public static TraceEvent fallbackFragmentStarted(final Position position) {
        return new TraceEvent(FALLBACK_STARTED, position, "", false, "");
    }

    public static TraceEvent fragmentCompleted(final Position position, final String source, final boolean available) {
        return new TraceEvent(COMPLETED, position, source, available, "");
    }

    public static TraceEvent fallbackFragmentCompleted(final Position position, final String source, final boolean available) {
        return new TraceEvent(FALLBACK_COMPLETED, position, source, available, "");
    }

    public static TraceEvent error(final Position position, final String source, final Throwable t) {
        return new TraceEvent(ERROR, position, source, false, t.getMessage());
    }

    public static TraceEvent error(final Position position, final String source, final String reason) {
        return new TraceEvent(ERROR, position, source, false, reason);
    }

    public static TraceEvent exception(final Position position, final Throwable t) {
        return new TraceEvent(ERROR, position, "", false, t.getMessage());
    }

    public final long getTimestamp() {
        return timestamp;
    }

    public final EventType getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    public String getSource() {
        return source;
    }

    public boolean isNonEmptyContent() {
        return nonEmptyContent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
