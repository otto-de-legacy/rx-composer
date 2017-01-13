package de.otto.rx.composer.page;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Headers;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.content.SingleContent;
import de.otto.rx.composer.providers.ContentProvider;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.fragment;
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
        final Fragment fragment = fragment(X, (position, parameters) -> just(someContent("Yeah!")));
        // when
        final Observable<Content> result = fragment.fetchWith(emptyParameters());
        // followedBy
        final Content content = result.toBlocking().single();
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final Fragment fragment = fragment(X, (position, parameters) -> {throw new IllegalStateException("Bumm!!!");});
        // when
        final Observable<Content> result = fragment.fetchWith(emptyParameters());
        // followedBy
        final Content content = result.toBlocking().single();
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));
    }

    private Content someContent(final String body) {
        return new SingleContent() {
            @Override
            public String getSource() {
                return body;
            }

            @Override
            public Position getPosition() {
                return X;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public Headers getHeaders() {
                return Headers.emptyHeaders();
            }

            @Override
            public LocalDateTime getCreated() {
                return LocalDateTime.now();
            }

        };
    }


}