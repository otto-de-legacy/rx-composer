package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.HttpContent;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.http.HttpClient;
import org.slf4j.Logger;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static javax.ws.rs.core.MediaType.valueOf;
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

    private final HttpClient httpClient;
    private final UriTemplate uriTemplate;
    private final String url;
    private final MediaType accept;

    HttpGetContentProvider(final HttpClient httpClient,
                           final UriTemplate uriTemplate,
                           final String accept) {
        checkNotNull(uriTemplate, "uriTemplate must not be null.");
        this.httpClient = httpClient;
        this.uriTemplate = uriTemplate;
        this.url = null;
        this.accept = valueOf(accept);
    }

    HttpGetContentProvider(final HttpClient httpClient,
                           final String url,
                           final String accept) {
        checkNotNull(url, "url must not be null.");
        this.httpClient = httpClient;
        this.url = url;
        this.uriTemplate = null;
        this.accept = valueOf(accept);
    }

    @Override
    public Observable<Content> getContent(final Position position,
                                          final Parameters parameters) {
        final String url = this.uriTemplate != null
                ? resolveUrl(parameters)
                : this.url;
        return httpClient
                .get(url, accept)
                .doOnNext(c -> LOG.trace("Got next content for position {} from {}", position, url))
                .doOnError(t -> LOG.error("Error fetching content {} for position {}: {}", url, position, t.getMessage()))
                .map(response -> (Content) new HttpContent(url, position, response))
                .onErrorReturn(e -> errorContent(position, e))
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
