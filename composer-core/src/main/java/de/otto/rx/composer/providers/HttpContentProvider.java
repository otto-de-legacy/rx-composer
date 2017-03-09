package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.rx.composer.client.ClientConfig;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.HttpContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.tracer.TraceEvent;
import de.otto.rx.composer.tracer.Tracer;
import org.slf4j.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.tracer.TraceEvent.*;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static javax.ws.rs.core.MediaType.valueOf;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A ContentProvider that is fetching content using HTTP GET.
 * <p>
 *     Both full URLs and UriTemplates are supported. UriTemplates are expanded using
 *     the {@link Parameters} when {@link #getContent(Position, Tracer, Parameters) getting content}.
 * </p>
 * <p>
 *     The HttpContentProvider supports resilient access to other services. Depending on the {@link ServiceClient}
 *     configuration, timeouts, retries and fallbacks are supported by this implementation.
 * </p>
 */
final class HttpContentProvider implements ContentProvider {

    private static final Logger LOG = getLogger(HttpContentProvider.class);

    private final ServiceClient serviceClient;
    private final UriTemplate uriTemplate;
    private final String url;
    private final MediaType accept;
    private final ContentProvider fallback;

    HttpContentProvider(final ServiceClient serviceClient,
                        final UriTemplate uriTemplate,
                        final String accept,
                        final ContentProvider fallback) {
        this.serviceClient = checkNotNull(serviceClient, "serviceClient must not be null.");
        this.uriTemplate = checkNotNull(uriTemplate, "uriTemplate must not be null.");
        this.url = null;
        this.accept = accept != null ? valueOf(accept) : WILDCARD_TYPE;
        if (fallback != null && !serviceClient.getClientConfig().isResilient()) {
            throw new IllegalArgumentException("Unable to configure a fallback with non-resilient service clients.");
        }
        this.fallback = fallback;
    }

    HttpContentProvider(final ServiceClient serviceClient,
                        final String url,
                        final String accept,
                        final ContentProvider fallback) {

        this.serviceClient = checkNotNull(serviceClient, "serviceClient must not be null.");
        this.url = checkNotNull(url, "url must not be null.");
        this.uriTemplate = null;
        this.accept = accept != null ? valueOf(accept) : WILDCARD_TYPE;
        if (fallback != null && !serviceClient.getClientConfig().isResilient()) {
            throw new IllegalArgumentException("Unable to configure a fallback with non-resilient service clients.");
        }
        this.fallback = fallback;
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Tracer tracer,
                                          final Parameters parameters) {
        final String url = this.uriTemplate != null
                ? resolveUrl(parameters)
                : this.url;
        final TraceEvent traceEvent = fragmentStarted(position, url);
        tracer.trace(traceEvent);
        final Observable<Content> contentObservable = serviceClient
                .get(url, accept)
                .subscribeOn(Schedulers.io())
                .doOnNext(response -> {

                    if (response.getStatusInfo().getFamily() == SERVER_ERROR) {
                        /*
                        Throw Exception so the circuit breaker is able to open the circuit,
                        retry execution or return the fallback value.
                        Don't do this for CLIENT_ERRORS as this is unlikely to be helpful in
                        most situations.
                         */
                        tracer.trace(error(position, url, "HTTP Server Error: " + response.getStatusInfo().toString()));
                        throw new ServerErrorException(response);
                    }
                })
                .map(response -> {
                    final Content content = new HttpContent(url, position, response, traceEvent.getTimestamp());
                    tracer.trace(fragmentCompleted(position, url, content.isAvailable()));
                    return content;
                });

        final ClientConfig clientConfig = serviceClient.getClientConfig();

        if (clientConfig.isResilient()) {
            final Observable<Content> observable;
                observable = HystrixObservableContent.from(
                        contentObservable.retry(clientConfig.getRetries()),
                        getFallbackObservable(position, tracer, parameters),
                        clientConfig.getRef(), clientConfig.getReadTimeout());
            return observable
                    .doOnError(t -> tracer.trace(exception(position, url, t)))
                    .filter(Content::isAvailable);
        } else {
            return contentObservable
                    .doOnError(t -> tracer.trace(exception(position, url, t)))
                    .filter(Content::isAvailable);
        }
    }

    /**
     * The observable content used as a fallback if the actual content is not available, or null if there is no fallback.
     * <p>
     *     The fallback content observable is enhanced by adding proper Tracer callbacks, so we can gather statistics
     *     about fallbacks as well as other content.
     * </p>
     * @param position the fragment position of the observed contents.
     * @param tracer the Tracer used to trace execution of the fallback
     * @param parameters parameters used to request fallback content.
     * @return observable fallback content
     */
    private Observable<Content> getFallbackObservable(final Position position,
                                                      final Tracer tracer,
                                                      final Parameters parameters) {
        if (fallback == null) {
            return null;
        } else {
            return fallback
                    .getContent(position, tracer, parameters)
                    .doOnSubscribe(() -> tracer.trace(
                            fallbackFragmentStarted(position)))
                    .doOnNext(fallbackContent -> tracer.trace(
                            fallbackFragmentCompleted(position, fallbackContent.getSource(), fallbackContent.isAvailable())))
                    .doOnError(t -> tracer.trace(
                            exception(position, url, t)));
        }
    }

    /**
     * Expands the {@code uriTemplate} using the given parameters and returns the URI.
     * <p>
     *     If the uriTemplate needs more variables than available in the parameters, the URI can not be created. In
     *     this case, an IllegalArgumentException is thrown and fetching the content will fail.
     * </p>
     * @param parameters parameters used to expand the uriTemplate
     * @return URI
     * @throws IllegalArgumentException if parameters are missing
     */
    private String resolveUrl(final Parameters parameters) {
        final String url = uriTemplate.expand(parameters.asImmutableMap());
        final String[] missingTemplateVariables = fromTemplate(url).getVariables();
        if (missingTemplateVariables != null && missingTemplateVariables.length > 0) {
            throw new IllegalArgumentException(
                    "Missing URI template variables in parameters. " +
                    "Unable to resolve " + Arrays.toString(missingTemplateVariables)
            );
        }
        return url;
    }

}
