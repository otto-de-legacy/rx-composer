package de.otto.rx.composer.tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.otto.rx.composer.tracer.LoggingTracer.LogLevel;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static de.otto.rx.composer.tracer.StatisticsTracer.statisticsTracer;

/**
 * A builder to configure the {@link Tracer} used during processing of a page.
 */
public class TracerBuilder {

    private boolean logging = false;
    private Logger logger = LoggerFactory.getLogger(LoggingTracer.class);
    private LogLevel defaultLogLevel = LogLevel.TRACE;
    private LogLevel errorLogLevel = LogLevel.ERROR;
    private Tracer delegate = noOpTracer();

    /**
     * Builds a default Tracer that is gathering {@link StatisticsTracer statistics} and logs messages
     * using {@link LoggingTracer}
     *
     * @return a LoggingTracer that is delegating to StatisticsTracer
     */
    public static Tracer loggingStatisticsTracer() {
        return tracerBuilder().withLogging().withDelegate(statisticsTracer()).build();
    }

    /**
     * Builds a default LoggingTracer.
     *
     * @return tracerBuilder().withLogging().build();
     */
    public static Tracer defaultLoggingTracer() {
        return tracerBuilder().withLogging().build();
    }

    /**
     * Builds a TracerBuilder with logging enabled.
     *
     * @return tracerBuilder().withLogging();
     */
    public static TracerBuilder loggingTracerBuilder() {
        return tracerBuilder().withLogging();
    }

    /**
     * Returns a new TracerBuilder instance.
     *
     * @return TracerBuilder
     */
    public static TracerBuilder tracerBuilder() {
        return new TracerBuilder();
    }

    /**
     * Specifies the Logger instance used to log trace events.
     * <p>
     *     By default, {@code LoggerFactory.getLogger(LoggingTracer.class} is used.
     * </p>
     * @param logger the SLF4J logger
     * @return this
     */
    public TracerBuilder withLogger(final Logger logger) {
        this.logger = logger;
        this.logging = true;
        return this;
    }

    /**
     * Specifies the log level used for default log messages.
     * <p>
     *     Default is {@link LogLevel#TRACE}
     * </p>
     * @param logLevel default LogLevel
     * @return this
     */
    public TracerBuilder withDefaultLogLevel(final LogLevel logLevel) {
        this.defaultLogLevel = logLevel;
        this.logging = true;
        return this;
    }

    /**
     * Specifies the log level used for error log messages.
     * <p>
     *     Default is {@link LogLevel#ERROR}
     * </p>
     * @param logLevel error LogLevel
     * @return this
     */
    public TracerBuilder withErrorLogLevel(final LogLevel logLevel) {
        this.errorLogLevel = logLevel;
        this.logging = true;
        return this;
    }

    /**
     * Specifies the delegate that is used by {@link LoggingTracer}.
     * <p>
     *     Default value is {@link NoOpTracer#noOpTracer()}
     * </p>
     * @param delegate delegate Tracer provided to the LoggingTracer.
     * @return this
     */
    public TracerBuilder withDelegate(final Tracer delegate) {
        this.delegate = delegate;
        return this;
    }

    /**
     * Enables the {@link LoggingTracer logging} of {@link TraceEvent trace events}.
     *
     * @return this
     */
    public TracerBuilder withLogging() {
        this.logging = true;
        return this;
    }

    /**
     * Builds the configured Tracer instance.
     *
     * @return Tracer
     */
    public Tracer build() {
        if (logging) {
            return new LoggingTracer(logger, defaultLogLevel, errorLogLevel, delegate);
        } else {
            return delegate;
        }
    }
}
