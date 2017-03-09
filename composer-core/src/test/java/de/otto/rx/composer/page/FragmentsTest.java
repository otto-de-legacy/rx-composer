package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.providers.ContentProvider;
import de.otto.rx.composer.tracer.Tracer;
import org.junit.Test;
import rx.Observable;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.followedBy;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class FragmentsTest {

    @Test
    public void shouldSetPosition() {
        // given
        final Fragment fragment = fragment(X, mock(ContentProvider.class));
        // then
        assertThat(fragment.getPosition(), is(X));
    }

    @Test
    public void shouldBuildSingleFragmentUsingForPos() {
        final Fragment fragment = fragment(X, mock(ContentProvider.class));
        assertThat(fragment, is(instanceOf(SingleFragment.class)));
    }

    @Test
    public void shouldBuildCompositeFragmentUsingForPos() {
        final ContentProvider fetchInitial = mock(ContentProvider.class);
        final ContentProvider thenFetch = mock(ContentProvider.class);
        final Fragment fragment = fragment(
                X,
                fetchInitial,
                followedBy(
                        (c)-> emptyParameters(),
                        fragment(Y, thenFetch)));
        assertThat(fragment, is(instanceOf(CompositeFragment.class)));
    }

    @Test
    public void shouldExecuteFragment() {
        // given
        final Tracer tracer = noOpTracer();
        final Content mockContent = mock(Content.class);
        when(mockContent.isAvailable()).thenReturn(true);
        when(mockContent.getBody()).thenReturn("Foo");
        // and
        final ContentProvider mockProvider = mock(ContentProvider.class);
        when(mockProvider.getContent(X, tracer, emptyParameters())).thenReturn(just(mockContent));
        // and
        final Fragment fragment = fragment(X, mockProvider);
        // when Fragment
        final Observable<Content> result =  fragment.fetchWith(tracer, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildCompositeWithoutContinuation() {
        fragment(
                X,
                mock(ContentProvider.class),
                new CompositeFragment.FragmentContinuation((c)->emptyParameters(), ImmutableList.of()));
    }

}