package de.otto.rx.composer.tracer;

import de.otto.rx.composer.content.Position;

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

    TraceEvent(final EventType type, final Position position, final String source, final boolean nonEmptyContent) {
        this.type = type;
        this.position = position;
        this.source = source;
        this.nonEmptyContent = nonEmptyContent;
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

}
