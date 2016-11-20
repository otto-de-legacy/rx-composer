package de.otto.rx.composer.steps;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.providers.ContentProvider;
import de.otto.rx.composer.providers.FetchOneOfManyContentProviderTest;
import org.junit.Test;
import rx.Observable;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchFirst;
import static de.otto.rx.composer.steps.Steps.forPos;
import static de.otto.rx.composer.steps.Steps.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class StepsTest {

    @Test
    public void shouldSetPosition() {
        // given
        final Step step = forPos(X, mock(ContentProvider.class));
        // then
        assertThat(step.getPosition(), is(X));
    }

    @Test
    public void shouldBuildSingleStepUsingForPos() {
        final Step step = forPos(X, mock(ContentProvider.class));
        assertThat(step, is(instanceOf(SingleStep.class)));
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
        assertThat(step, is(instanceOf(CompositeStep.class)));
    }

    @Test
    public void shouldExecuteStep() {
        // given
        final Content mockContent = mock(Content.class);
        when(mockContent.getAvailability()).thenReturn(AVAILABLE);
        when(mockContent.getBody()).thenReturn("Foo");
        // and
        final ContentProvider mockProvider = mock(ContentProvider.class);
        when(mockProvider.getContent(X, emptyParameters())).thenReturn(just(mockContent));
        // and
        final Step step = forPos(X, mockProvider);
        // when Step
        final Observable<Content> result =  step.execute(emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildCompositeWithoutContinuation() {
        forPos(
                X,
                mock(ContentProvider.class),
                new CompositeStep.StepContinuation((c)->emptyParameters(), ImmutableList.of()));
    }

}