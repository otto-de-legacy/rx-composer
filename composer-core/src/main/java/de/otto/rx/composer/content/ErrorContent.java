package de.otto.rx.composer.content;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.providers.ContentProvider;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import java.util.EnumSet;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static java.util.EnumSet.of;
import static javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR;
import static javax.ws.rs.core.Response.Status.Family.SERVER_ERROR;

/**
 * Content that is representing an error.
 * <p>
 *     ErrorContent is never {@link #isAvailable() available} and has no {@link #getBody()}, even if some
 *     HTTP error response returned something in it's body.
 * </p>
 */
public final class ErrorContent extends SingleContent {

    /**
     * The source of the error.
     */
    public enum ErrorSource {
        /**
         * The error occured because of an HTTP Client Error (HTTP 4xx).
         */
        CLIENT_ERROR,
        /**
         * The error occured because of an HTTP Server Error (HTTP 5xx).
         */
        SERVER_ERROR,
        /**
         * The source of the error is an exception. In this case {@link #getThrowable()} will return a
         * non-empty Throwable.
         */
        EXCEPTION,
        /**
         * Some unspecified error. Neither an HTTP error, nor an exception is the reason of the error.
         */
        OTHER
    }

    /**
     * HTTP error codes accepted by ErrorContent.
     */
    private static final EnumSet<Family> HTTP_ERRORS = of(CLIENT_ERROR, SERVER_ERROR);

    private final String source;
    private final ErrorSource errorSource;
    private final Position position;
    private final Throwable e;
    private final String errorReason;
    private final long startedTs;
    private final long completedTs = System.currentTimeMillis();

    private ErrorContent(final String source,
                         final Position position,
                         final Throwable e,
                         final long startedTs) {
        this.source = source;
        this.startedTs = startedTs;
        this.position = position;
        this.e = e;
        this.errorReason = e.getMessage();
        this.errorSource = ErrorSource.EXCEPTION;
    }

    private ErrorContent(final String source,
                         final Position position,
                         final String errorReason,
                         final ErrorSource errorSource,
                         final long startedTs) {
        this.source = source;
        this.startedTs = startedTs;
        this.position = position;
        this.e = null;
        this.errorReason = errorReason;
        this.errorSource = errorSource;
    }

    /**
     * Creates an ErrorContent with {@link ErrorSource#EXCEPTION}.
     *
     * @param position the content position.
     * @param e the Throwable. Must not be null.
     * @param startedTs the timestamp when fetching the content has started.
     * @return ErrorContent
     */
    public static ErrorContent errorContent(final Position position,
                                            final Throwable e,
                                            final long startedTs) {
        return new ErrorContent("<unknown source>", position, e, startedTs);
    }

    /**
     * Creates an ErrorContent with {@link ErrorSource#OTHER}.
     *
     * @param position the content position.
     * @param errorReason the reason describing the error.
     * @param startedTs the timestamp when fetching the content has started.
     * @return ErrorContent
     */
    public static ErrorContent errorContent(final String source,
                                            final Position position,
                                            final String errorReason,
                                            final long startedTs) {
        return new ErrorContent(source, position, errorReason, ErrorSource.OTHER, startedTs);
    }

    /**
     * Creates an ErrorContent for a HTTP {@link ErrorSource#CLIENT_ERROR client}- or
     * {@link ErrorSource#SERVER_ERROR server} error.
     *
     * @param position the content position
     * @param response the HTTP response. This must either by a client-error response or a server-error response.
     * @param startedTs the timestamp when fetching the content has started.
     * @return ErrorContent
     * @throws IllegalArgumentException if the response is not a client- or server error response.
     */
    public static ErrorContent httpErrorContent(final String source,
                                                final Position position,
                                                final Response response,
                                                final long startedTs) {
        final StatusType statusInfo = response.getStatusInfo();
        final Family family = statusInfo.getFamily();

        checkArgument(HTTP_ERRORS.contains(family),
                "Response is not a HTTP client or server error");

        final ErrorSource errorSource = family == CLIENT_ERROR
                ? ErrorSource.CLIENT_ERROR
                : ErrorSource.SERVER_ERROR;

        return new ErrorContent(source, position, statusInfo.getReasonPhrase(), errorSource, startedTs);
    }

    /**
     * Returns the config of the Content.
     * <p>
     * For HTTP Content, this is the URL. In other cases, some other unique config key should be used,
     * as this method is used to track the behaviour during execution.
     * </p>
     *
     * @return source of the content
     */
    @Override
    public String getSource() {
        return source != null ? source : "";
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

    /**
     * @return true, if the content is an error content, false otherwise.
     */
    @Override
    public boolean isErrorContent() {
        return true;
    }

    /**
     * If this content is an {@link #isErrorContent()}  error content}, {@link ErrorContent} is returned.
     *
     * @return ErrorContent
     * @throws IllegalStateException if this is not an error content.
     */
    @Override
    public ErrorContent asErrorContent() {
        return this;
    }

    /**
     * Returns the source of the error.
     *
     * @return ErrorSource
     */
    public ErrorSource getErrorSource() {
        return errorSource;
    }

    /**
     * Returns a message specifying the reason of the error.
     *
     * @return error reason
     */
    public String getErrorReason() {
        return errorReason;
    }

    /**
     * Returns the throwable that caused the error. If the throwable is available,
     * {@link #getErrorSource() error source} will always be {@link ErrorSource#EXCEPTION}.
     *
     * @return optional throwable that caused the error.
     */
    public Optional<Throwable> getThrowable() {
        return Optional.ofNullable(e);
    }

    @Override
    public String toString() {
        return "ErrorContent{" +
                "source='" + source + '\'' +
                ", errorSource=" + errorSource +
                ", position=" + position +
                ", e=" + e +
                ", errorReason='" + errorReason + '\'' +
                ", startedTs=" + startedTs +
                ", completedTs=" + completedTs +
                '}';
    }
}
