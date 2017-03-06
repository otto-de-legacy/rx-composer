package de.otto.rx.composer.page;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.ErrorContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.context.RequestContext;
import de.otto.rx.composer.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static rx.Observable.just;

/**
 * A single Fragment in a Plan to retrieve content using a {@link ContentProvider}.
 */
class SingleFragment implements Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(SingleFragment.class);

    /**
     * The {@link ContentProvider} is responsible for getting Observable {@link Content}.
     */
    private final ContentProvider contentProvider;
    /**
     * The Fragment's Position in the Plan.
     */
    private final Position position;

    /**
     * Create a SingleFragment instance for a Position using a ContentProvider.
     *
     * @param position the resulting Content's Position.
     * @param contentProvider the ContentProvider used to actually fetch the Content.
     */
    SingleFragment(final Position position,
                   final ContentProvider contentProvider) {
        this.position = position;
        this.contentProvider = contentProvider;
    }

    /**
     * {@inheritDoc}
     * <p>
     *     This implementation is forwarding the Parameters to the ContentProvider.
     *     Exceptions are logged and returned as an {@link ErrorContent}.
     * </p>
     */
    @Override
    public Observable<Content> fetchWith(final RequestContext context, final Parameters parameters) {
        try {
            return contentProvider
                    .getContent(position, context, parameters)
                    .onErrorReturn(e -> {
                        final ErrorContent errorContent = errorContent(position, e);
                        context.traceError(errorContent);
                        return errorContent;
                    })
                    .filter(Content::isAvailable);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            final ErrorContent errorContent = errorContent(position, e);
            context.traceException(errorContent);
            return just(errorContent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getPosition() {
        return position;
    }
}
