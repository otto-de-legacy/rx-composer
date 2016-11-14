package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.ErrorContent;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static rx.Observable.just;

/**
 * A single fetch in a Plan to retrieve content.
 */
class SingleStep implements Step {

    private static final Logger LOG = LoggerFactory.getLogger(SingleStep.class);

    private final ContentProvider contentProvider;

    private final Position position;

    SingleStep(final Position position,
               final ContentProvider contentProvider) {
        this.position = position;
        this.contentProvider = contentProvider;
    }

    @Override
    public Observable<Content> execute(final Parameters parameters) {
        try {
            return contentProvider
                    .getContent(position, 0, parameters)
                    .doOnError((t) -> LOG.error(t.getMessage(), t))
                    .onErrorReturn((e) -> new ErrorContent(position, 0, e));
        } catch (final Exception e) {
            return just(new ErrorContent(position, 0, e));
        }
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
