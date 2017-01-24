package de.otto.rx.composer.providers;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import rx.Observable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.providers.HystrixWrapper.from;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A ContentProvider that is fetching content using HTTP GET.
 * <p>
 *     Both full URLs and UriTemplates are supported. UriTemplates are expanded using
 *     the {@link Parameters} when {@link #getContent(Position, Parameters) getting content}.
 * </p>
 */
final class CircuitBreakingContentProvider implements ContentProvider {

    private static final Logger LOG = getLogger(CircuitBreakingContentProvider.class);

    private final ContentProvider delegate;
    private final String commandKey;
    private final int timeoutMillis;

    CircuitBreakingContentProvider(final ContentProvider contentProvider,
                                   final int timeoutMillis,
                                   final String commandKey) {
        checkNotNull(contentProvider, "contentProvider must not be null.");
        checkNotNull(commandKey, "commandKey must not be null.");
        checkArgument(timeoutMillis >= 0, "timeoutMillis must not be negative");
        this.delegate = contentProvider;
        this.timeoutMillis = timeoutMillis;
        this.commandKey = commandKey;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        final Observable<Content> contentObservable = delegate.getContent(position, parameters);
        return from(contentObservable, commandKey, timeoutMillis)
                .doOnError(t -> LOG.error("Caught Exception from CircuitBreaker: for position {}: {} ({})", position, t.getCause().getMessage(), t.getMessage()))
                .filter(Content::isAvailable);
    }

}
