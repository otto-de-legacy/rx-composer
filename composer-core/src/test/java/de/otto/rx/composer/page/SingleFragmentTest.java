package de.otto.rx.composer.page;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Headers;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.content.SingleContent;
import de.otto.rx.composer.providers.ContentProvider;
import org.junit.Test;
import rx.Observable;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.StaticTextContent.staticTextContent;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static rx.Observable.just;

public class SingleFragmentTest {

    @Test
    public void shouldBuildSingleFragmentUsingForPos() {
        final Fragment fragment = fragment(X, mock(ContentProvider.class));
        assertThat(fragment, is(instanceOf(SingleFragment.class)));
    }

    @Test
    public void shouldFetchContent() {
        // given
        final Fragment fragment = fragment(X, (position, ctx, parameters) -> just(someContent("Yeah!")));
        // when
        final Observable<Content> result = fragment.fetchWith(noOpTracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Fragment fragment = fragment(X, (position, ctx, parameters) -> {throw new IllegalStateException("Bumm!!!");});
        // when
        final Observable<Content> result = fragment.fetchWith(noOpTracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));
    }

    private Content someContent(final String body) {
        return staticTextContent(body, X, body);
    }


}