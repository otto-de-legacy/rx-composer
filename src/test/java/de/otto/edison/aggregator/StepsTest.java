package de.otto.edison.aggregator;

import org.junit.Test;

import static de.otto.edison.aggregator.AbcPosition.X;
import static de.otto.edison.aggregator.Steps.fetch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class StepsTest {

    @Test
    public void shouldBuildFetchStep() {
        final Step step = fetch(X, mock(ContentProvider.class));
        assertThat(step.getPosition(), is(X));
    }
}