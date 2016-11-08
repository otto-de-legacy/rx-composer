package de.otto.edison.aggregator;

import rx.Observable;

/**
 * A single fetch in a Plan to retrieve content.
 */
public class Step {
    private ContentProvider contentProvider;

    private final ContentPosition contentPosition;

    private Step(final ContentPosition contentPosition,
                 final ContentProvider contentProvider) {
        this.contentPosition = contentPosition;
        this.contentProvider = contentProvider;
    }

    public Observable<? extends Content> execute(final Parameters parameters) {
        return contentProvider.getContent(contentPosition, parameters);
    }

    public static Step fetch(final ContentPosition contentPosition, final ContentProvider contentProvider)   {
        return new Step(contentPosition, contentProvider);
    }
}
