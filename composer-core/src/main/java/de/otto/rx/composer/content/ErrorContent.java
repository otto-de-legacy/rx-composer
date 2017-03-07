package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.providers.ContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.otto.rx.composer.content.Headers.emptyHeaders;

public final class ErrorContent extends SingleContent {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorContent.class);

    private final Position position;
    private final Throwable e;
    private final long startedTs;
    private final long completedTs = System.currentTimeMillis();

    private ErrorContent(final Position position, final Throwable e, final long startedTs) {
        this.startedTs = startedTs;
        this.position = position;
        this.e = e;
    }

    public static ErrorContent errorContent(final Position position, final Throwable e, final long startedTs) {
        return new ErrorContent(position, e, startedTs);
    }

    /**
     * Returns the config of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique config key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     * <p>
     *     ErrorContent does not know about the 'real' config, position.name() is used instead.
     * </p>
     * @return config identifier
     */
    @Override
    public String getSource() {
        return position.name();
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
     * @return true, if content is available and not empty, false otherwise.
     */
    @Override
    public boolean isAvailable() {
        return false;
    }

    /**
     * The body of the content element, as returned from the {@link ContentProvider}
     *
     * @return body or empty String
     */
    @Override
    public String getBody() {
        return "";
    }

    /**
     * Return meta-information about the content returned from a {@link ContentProvider}.
     * <p>
     * For HttpContent, the headers are the HTTP response headers returned from the called service.
     * </p>
     *
     * @return response headers.
     */
    @Override
    public Headers getHeaders() {
        return emptyHeaders();
    }

    @Override
    public long getStartedTs() {
        return startedTs;
    }

    @Override
    public long getCompletedTs() {
        return completedTs;
    }

    public Throwable getThrowable() {
        return e;
    }
}
