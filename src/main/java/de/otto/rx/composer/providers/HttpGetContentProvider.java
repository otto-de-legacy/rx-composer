package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.rx.composer.client.ClientConfig;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.HttpContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.providers.HystrixWrapper.from;
import static javax.ws.rs.core.MediaType.valueOf;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A ContentProvider that is fetching content using HTTP GET.
 * <p>
 *     Both full URLs and UriTemplates are supported. UriTemplates are expanded using
 *     the {@link Parameters} when {@link #getContent(Position, Parameters) getting content}.
 * </p>
 */
final class HttpGetContentProvider implements ContentProvider {

    private static final Logger LOG = getLogger(HttpGetContentProvider.class);

    private final ServiceClient serviceClient;
    private final UriTemplate uriTemplate;
    private final String url;
    private final MediaType accept;
    private final ContentProvider fallback;

    HttpGetContentProvider(final ServiceClient serviceClient,
                           final UriTemplate uriTemplate,
                           final String accept,
                           final ContentProvider fallback) {
        checkNotNull(uriTemplate, "uriTemplate must not be null.");
        this.serviceClient = serviceClient;
        this.uriTemplate = uriTemplate;
        this.url = null;
        this.accept = valueOf(accept);
        if (fallback != null && !serviceClient.getClientConfig().isResilient()) {
            throw new IllegalArgumentException("Unable to configure a fallback with non-resilient service clients.");
        }
        this.fallback = fallback;
    }

    HttpGetContentProvider(final ServiceClient serviceClient,
                           final String url,
                           final String accept,
                           final ContentProvider fallback) {
        checkNotNull(url, "url must not be null.");
        this.serviceClient = serviceClient;
        this.url = url;
        this.uriTemplate = null;
        this.accept = valueOf(accept);
        if (fallback != null && !serviceClient.getClientConfig().isResilient()) {
            throw new IllegalArgumentException("Unable to configure a fallback with non-resilient service clients.");
        }
        this.fallback = fallback;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        final String url = this.uriTemplate != null
                ? resolveUrl(parameters)
                : this.url;
        final Observable<Content> contentObservable = serviceClient
                .get(url, accept)
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> {
                    LOG.trace("Got next content for position {} from {}", position, url);
                    if (response.getStatusInfo().getFamily() == SERVER_ERROR) {
                        /*
                        Throw Exception so the circuit breaker is able to open the circuit,
                        retry execution or return the fallback value.
                        Don't do this for CLIENT_ERRORS as this is unlikely to be helful in
                        most situations.
                         */
                        throw new ServerErrorException(response);
                    }
                })
                .map(response -> (Content) new HttpContent(url, position, response));

        final ClientConfig clientConfig = serviceClient.getClientConfig();

        if (clientConfig.isResilient()) {
            final Observable<Content> observable = from(
                    contentObservable.retry(clientConfig.getRetries()),
                    fallback != null ? fallback.getContent(position, parameters) : null,
                    clientConfig.getKey(), clientConfig.getReadTimeout());
            return observable
                    .doOnError(t -> LOG.error("Caught Exception from CircuitBreaker: for position {}: {} ({})", position, t.getCause().getMessage(), t.getMessage()))
                    .filter(Content::isAvailable);
        } else {
            return contentObservable
                    .doOnError(t -> LOG.error("Error fetching content {} for position {}: {}", url, position, t.getMessage()))
                    .filter(Content::isAvailable);
        }
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
