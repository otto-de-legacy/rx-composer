package de.otto.rx.composer.context;

import de.otto.rx.composer.content.ErrorContent;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.time.Instant;

import static java.time.Instant.now;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Context of a single page request, used for gathering request statistics and debug information.
 */
public class RequestContext {
    private static final Logger LOG = getLogger(RequestContext.class);

    private final Instant started = now();

    public void traceException(ErrorContent errorContent) {

    }

    public void traceError(ErrorContent errorContent) {

    }

    public void traceError(Position position, String url, Throwable t) {
        LOG.error("Error fetching content {} for position {}: {}", url, position.name(), t.getMessage());
    }

    public void traceSubscribe(final Position position, final String url) {
        LOG.trace("Start fetching content for position {} from {}", position.name(), url);
    }

    public void traceNext(Position position, String url, Response response) {
        LOG.trace("Got next content for position {} from {} with status {}", position.name(), url, response.getStatus());
    }

    public void traceCircuitBreakerException(final Position position, final Throwable t) {
        LOG.error("Caught Exception from CircuitBreaker: for position {}: {} ({})", position.name(), t.getCause().getMessage(), t.getMessage());
    }
}
