package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;

import javax.ws.rs.core.Response;
import java.util.Objects;

import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static de.otto.rx.composer.content.ErrorContent.httpErrorContent;
import static de.otto.rx.composer.content.Headers.of;
import static java.lang.String.format;

public final class HttpContent extends SingleContent {

    private final String source;
    private final Position position;
    private final String body;
    private final Headers headers;
    private final boolean available;
    private final long startedTs;
    private final long completedTs = System.currentTimeMillis();

    /**
     * Create a HttpContent element, representing {@link Content} retrieved from a (micro)service.
     *
     * @param source The URI of the requested service
     * @param position The content position inside of the {@link Page}.
     * @param response The HTTP response returned from a different (micro)service.
     */
    private HttpContent(final String source,
                        final Position position,
                        final Response response,
                        final long startedTs) {
        this.source = source;
        this.position = position;
        this.body = response.readEntity(String.class);
        this.available = response.getStatus() < 300 && body != null && !body.isEmpty();
        this.headers = of(response.getStringHeaders());
        this.startedTs = startedTs;
    }

    /**
     * Create a Content element, representing {@link Content} retrieved from a (micro)service.
     * <p>
     *     Depending on the HTTP response status, an ErrorContent or HttpContent instance is created.
     * </p>
     *
     * @param source The URI of the requested service
     * @param position The content position inside of the {@link Page}.
     * @param response The HTTP response returned from a different (micro)service.
     */
    public static Content httpContent(final String source,
                                      final Position position,
                                      final Response response,
                                      final long startedTs) {
        final Content content;
        switch (response.getStatusInfo().getFamily()) {
            case CLIENT_ERROR:
            case SERVER_ERROR:
                content = httpErrorContent(source, position, response, startedTs);
                break;
            case OTHER:
            case INFORMATIONAL:
            case REDIRECTION:
                content = errorContent(
                        source,
                        position,
                        format("Unexpected HTTP response code %s: %s", response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase()),
                        startedTs);
                break;
            default:
                content = new HttpContent(source, position, response, startedTs);
                break;
        }
        return content;
    }

    /**
     * Returns the config of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique config key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     *
     * @return config identifier
     */
    @Override
    public String getSource() {
        return source;
    }

    /**
     * The content position inside of the {@link Page}
     *
     * @return Position
     */
    @Override
    public Position getPosition() {
        return position;
    }

    /**
     *
     * @return true, if content is available and not empty, false otherwise.
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * The body of the content element, as returned from the {@link de.otto.rx.composer.providers.ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return body != null ? body : "";
    }

    /**
     * Return meta-information about the content.
     * <p>
     *     For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public long getStartedTs() {
        return startedTs;
    }

    /**
     * The creation time stamp of the content element.
     * <p>
     *     Primarily used for logging purposes.
     * </p>
     *
     * @return completedTs ts
     */
    @Override
    public long getCompletedTs() {
        return completedTs;
    }

    @Override
    public String toString() {
        return "HttpContent{" +
                "source='" + source + '\'' +
                ", position=" + position +
                ", body='" + body + '\'' +
                ", headers=" + headers +
                ", available=" + available +
                ", startedTs=" + startedTs +
                ", completedTs=" + completedTs +
                '}';
    }
}
