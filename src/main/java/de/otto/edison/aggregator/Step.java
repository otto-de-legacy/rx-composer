package de.otto.edison.aggregator;

import rx.Observable;

/**
 * A single fetch in a Plan to retrieve content.
 */
public class Step {
    private ContentProvider contentProvider;

    private final Position position;

    Step(final Position position,
                 final ContentProvider contentProvider) {
        this.position = position;
        this.contentProvider = contentProvider;
    }

    public Observable<? extends Content> execute(final Parameters parameters) {
        return contentProvider.getContent(position, parameters);
    }

    public Position getPosition() {
        return position;
    }
}
