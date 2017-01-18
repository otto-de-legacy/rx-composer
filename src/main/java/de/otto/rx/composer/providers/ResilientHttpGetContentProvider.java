package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.HttpContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.http.HttpClient;
import org.slf4j.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.providers.HystrixWrapper.from;
import static javax.ws.rs.core.MediaType.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A ContentProvider that is fetching content using HTTP GET.
 * <p>
 *     Both full URLs and UriTemplates are supported. UriTemplates are expanded using
 *     the {@link Parameters} when {@link #getContent(Position, Parameters) getting content}.
 * </p>
 */
final class ResilientHttpGetContentProvider implements ContentProvider {

    private static final Logger LOG = getLogger(ResilientHttpGetContentProvider.class);

    private final HttpClient httpClient;
    private final UriTemplate uriTemplate;
    private final String url;
    private final MediaType accept;
    private final String commandKey;

    ResilientHttpGetContentProvider(final HttpClient httpClient,
                                    final UriTemplate uriTemplate,
                                    final String accept,
                                    final String commandKey) {
        checkNotNull(uriTemplate, "uriTemplate must not be null.");
        this.httpClient = httpClient;
        this.uriTemplate = uriTemplate;
        this.url = null;
        this.accept = valueOf(accept);
        this.commandKey = commandKey;
    }

    ResilientHttpGetContentProvider(final HttpClient httpClient,
                                    final String url,
                                    final String accept,
                                    final String commandKey) {
        checkNotNull(url, "url must not be null.");
        this.httpClient = httpClient;
        this.url = url;
        this.uriTemplate = null;
        this.accept = valueOf(accept);
        this.commandKey = commandKey;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        final String url = this.uriTemplate != null
                ? resolveUrl(parameters)
                : this.url;
        final Observable<Content> contentObservable = httpClient
                .get(url, accept)
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> {
                    LOG.trace("Got next content for position {} from {} with HTTP status {}", position, url, response.getStatusInfo().getStatusCode());
                    switch (response.getStatusInfo().getFamily()) {
                        case SERVER_ERROR:
                            throw new ServerErrorException(response);
                        case CLIENT_ERROR:
                            // TODO: ErrorContent for client errors?
                            throw new ClientErrorException(response);
                        default:
                            break;
                    }
                })
                .map(response -> (Content) new HttpContent(url, position, response));
        return from(contentObservable, commandKey, httpClient.getTimeoutMillis() + 50)
                .doOnError(t -> LOG.error("Error fetching content {} for position {}: {} ({})", url, position, t.getCause().getMessage(), t.getMessage()))
                .filter(Content::isAvailable);
    }

    private String resolveUrl(final Parameters parameters) {
        final String url = uriTemplate.expand(parameters.asImmutableMap());
        final String[] missingTemplateVariables = fromTemplate(url).getVariables();
        if (missingTemplateVariables != null && missingTemplateVariables.length > 0) {
            throw new IllegalArgumentException("Missing URI template variables in parameters. Unable to resolve " + Arrays.toString(missingTemplateVariables));
        }
        return url;
    }

}
