package de.otto.rx.composer.tracer;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Statistics;
import org.slf4j.Logger;

/**
 * {@inheritDoc}
 * <p>
 *     This implementation will write information about the {@link TraceEvent traced events} to a SLF4J Logger.
 *     It is implemented as a delegate, so you can combine logging with other Tracer implementations, even on a
 *     per-request base.
 * </p>
 * <p>
 *     The LogLevels for normal and error messages, and even the Logger used to write log messages can be configured.
 * </p>
 * <p>
 *     By default, LoggingTracer.class is used to create a Logger using the SLF4J LoggerFactory. The default LogLevel
 *     to trace events is {@link LogLevel#TRACE}, errors are logged user {@link LogLevel#ERROR}. If no
 *     delegate {@link Tracer} is provided, the {@link NoOpTracer} is used. In this case, {@link #getEvents()} will
 *     always return an empty list and the {@link #getStatistics() statistics} will remain empty.
 * </p>
 * <p>
 *     All these defauls can be overridden by the different constructors.
 * </p>
 */
public final class LoggingTracer implements Tracer {

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private final Logger logger;
    private final LogLevel defaultLogLevel;
    private final LogLevel errorLogLevel;
    private final Tracer delegate;

    /**
     * Creates a LoggingTracer that is writing messages to the {@code logger} using the specified
     * {@link LogLevel log levels}. All events are delegated to the given {@link Tracer delegate}.
     *
     * @param defaultLogLevel log level used to write normal messages
     * @param errorLogLevel log level used to write error messages
     */
    public LoggingTracer(final Logger logger,
                         final LogLevel defaultLogLevel,
                         final LogLevel errorLogLevel,
                         final Tracer delegate) {
        this.logger = logger;
        this.defaultLogLevel = defaultLogLevel;
        this.errorLogLevel = errorLogLevel;
        this.delegate = delegate;
    }

    public void trace(final TraceEvent event) {
        delegate.trace(event);

        switch (event.getType()) {
            case COMPLETED:
                trace("COMPLETED fetching {} content for position {} from {}", event.isNonEmptyContent() ? "AVAILABLE" : "UNAVAILABLE", event.getPosition().name(), event.getSource());
                break;
            case FALLBACK_COMPLETED:
                trace("COMPLETED fetching {} FALLBACK content for position {} from {}", event.isNonEmptyContent() ? "AVAILABLE" : "UNAVAILABLE", event.getPosition().name(), event.getSource());
                break;
            case ERROR:
                if (event.getSource().isEmpty()) {
                    error("ERROR fetching content for position: {}", event.getPosition().name());
                } else {
                    error("ERROR fetching content for position {} from {}: {}", event.getPosition().name(), event.getSource(), event.getErrorMessage());
                }
                break;
            case STARTED:
                trace("STARTED fetching content for position {} from {}", event.getPosition().name(), event.getSource());
                break;
            case FALLBACK_STARTED:
                trace("STARTED fetching FALLBACK content for position {} from {}", event.getPosition().name(), event.getSource());
                break;
            default:
                throw new IllegalStateException("Unknown EventType " + event.getType());
        }
    }

    public ImmutableList<TraceEvent> getEvents() {
        return delegate.getEvents();
    }

    public Statistics getStatistics() {
        return delegate.getStatistics();
    }

    private void trace(final String msg, final Object... arguments) {
        log(defaultLogLevel, msg, arguments);
    }

    private void error(final String msg, final Object... arguments) {
        log(errorLogLevel, msg, arguments);
    }

    private void log(final LogLevel logLevel, final String msg, final Object... arguments) {
        switch (logLevel) {
            case TRACE:
                logger.trace(msg, arguments);
                break;
            case DEBUG:
                logger.debug(msg, arguments);
                break;
            case INFO:
                logger.info(msg, arguments);
                break;
            case WARN:
                logger.warn(msg, arguments);
                break;
            case ERROR:
                logger.error(msg, arguments);
                break;
        }
    }

}
