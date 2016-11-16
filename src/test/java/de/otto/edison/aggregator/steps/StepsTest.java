package de.otto.edison.aggregator.steps;

import com.google.common.collect.ImmutableList;
import de.otto.edison.aggregator.providers.ContentProvider;
import org.junit.Test;

import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.AbcPosition.Y;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.steps.Steps.forPos;
import static de.otto.edison.aggregator.steps.Steps.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class StepsTest {

    @Test
    public void shouldBuildSingleStepUsingForPos() {
        final Step step = forPos(X, mock(ContentProvider.class));
        assertThat(step, is(instanceOf(SingleStep.class)));
        assertThat(step.getPosition(), is(X));
    }

    @Test
    public void shouldBuildCompositeStepUsingForPos() {
        final ContentProvider fetchInitial = mock(ContentProvider.class);
        final ContentProvider thenFetch = mock(ContentProvider.class);
        final Step step = forPos(
                X,
                fetchInitial,
                then(
                        (c)-> emptyParameters(),
                        forPos(Y, thenFetch)));
        assertThat(step.getPosition(), is(X));
        assertThat(step, is(instanceOf(CompositeStep.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildCompositeWithoutContinuation() {
        forPos(
                X,
                mock(ContentProvider.class),
                new CompositeStep.StepContinuation((c)->emptyParameters(), ImmutableList.of()));
    }

}