package de.otto.edison.aggregator.steps;

import de.otto.edison.aggregator.providers.ContentProvider;
import org.junit.Test;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.steps.Steps.fetch;
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