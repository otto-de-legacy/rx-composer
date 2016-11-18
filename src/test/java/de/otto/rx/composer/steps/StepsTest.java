package de.otto.rx.composer.steps;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.providers.ContentProvider;
import org.junit.Test;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.steps.Steps.forPos;
import static de.otto.rx.composer.steps.Steps.then;
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