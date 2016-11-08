package de.otto.edison.aggregator;

import rx.Observable;

/**
 * A single fetch in a Plan to retrieve content.
 */
public class SingleStep implements Step {
    private ContentProvider contentProvider;

    private final Position position;

    SingleStep(final Position position,
               final ContentProvider contentProvider) {
        this.position = position;
        this.contentProvider = contentProvider;
    }

    @Override
    public Observable<? extends Content> execute(final Parameters parameters) {
        return contentProvider.getContent(position, parameters);
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
