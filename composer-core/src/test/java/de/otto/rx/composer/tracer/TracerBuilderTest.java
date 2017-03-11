package de.otto.rx.composer.tracer;

import org.junit.Test;
import org.slf4j.Logger;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.tracer.TraceEvent.error;
import static de.otto.rx.composer.tracer.TraceEvent.fragmentStarted;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TracerBuilderTest {

    @Test
    public void shouldDelegateEvents() {
        final Tracer delegate = mock(Tracer.class);

        final Tracer tracer = TracerBuilder.tracerBuilder()
                .withDelegate(delegate)
                .withLogging()
                .build();

        tracer.trace(error(A, "source", "reason"));
        tracer.trace(fragmentStarted(A, "source"));

        verify(delegate, times(2)).trace(any(TraceEvent.class));
    }

    @Test
    public void shouldLogErrorsAsWarning() {
        final Logger logger = mock(Logger.class);

        final Tracer tracer = TracerBuilder.tracerBuilder()
                .withErrorLogLevel(LoggingTracer.LogLevel.WARN)
                .withLogger(logger)
                .build();

        tracer.trace(error(A, "source", "reason"));

        verify(logger).warn("ERROR fetching content for position {} from {}: {}", new Object[] {"A", "source", "reason"});
    }

    @Test
    public void shouldLogTracesAsInfo() {
        final Logger logger = mock(Logger.class);

        final Tracer tracer = TracerBuilder.tracerBuilder()
                .withDefaultLogLevel(LoggingTracer.LogLevel.INFO)
                .withLogger(logger)
                .build();

        tracer.trace(fragmentStarted(A, "source"));

        verify(logger).info("STARTED fetching content for position {} from {}", new Object[] {"A", "source"});
    }
}