package de.otto.rx.composer.tracer;

import org.junit.Test;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static de.otto.rx.composer.tracer.TraceEvent.error;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NoOpTracerTest {

    @Test
    public void shouldDoNothing() {
        final Tracer tracer = noOpTracer();
        tracer.trace(error(A, "some source", "some reaseon"));

        assertThat(tracer.getEvents(), is(emptyList()));
        assertThat(tracer.getStatistics().getNumErrors(), is(0));
        assertThat(tracer.getStatistics().getStartedTs(), is(0L));
    }

}