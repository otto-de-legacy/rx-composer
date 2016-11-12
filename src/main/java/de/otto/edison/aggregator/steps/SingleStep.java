package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import rx.Observable;

/**
 * A single fetch in a Plan to retrieve content.
 */
class SingleStep implements Step {

    private final ContentProvider contentProvider;

    private final Position position;

    SingleStep(final Position position,
               final ContentProvider contentProvider) {
        this.position = position;
        this.contentProvider = contentProvider;
    }

    @Override
    public Observable<Content> execute(final Parameters parameters) {
        return contentProvider.getContent(position, parameters);
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
