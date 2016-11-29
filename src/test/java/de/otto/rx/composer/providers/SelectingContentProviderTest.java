package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.Iterator;

import static com.google.common.collect.ImmutableList.*;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchAll;
import static de.otto.rx.composer.providers.ContentProviders.fetchFirst;
import static de.otto.rx.composer.providers.ContentProviders.fetchFirstMatching;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class SelectingContentProviderTest {

    @Test
    public void shouldFetchFirstWithContent() {
        // given
        final ContentProvider contentProvider = fetchFirst(of(
                (position, parameters) -> just(new TestContent(X, "Foo")),
                (position, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        final Observable<Content> result =  contentProvider.getContent(X, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldFetchAllWithContent() {
        // given
        final ContentProvider contentProvider = fetchAll(of(
                (position, parameters) -> just(new TestContent(X, "Foo")),
                (position, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        final Observable<Content> result =  contentProvider.getContent(X, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.isComposite(), is(true));
        assertThat(content.getBody(), is("Foo\nBar"));
    }

    @Test
    public void shouldFetchFirstMatchingPredicate() {
        // given
        final ContentProvider contentProvider = fetchFirstMatching(
                (content) -> content.getBody().contains("a"),
                of(
                        (position, parameters) -> just(new TestContent(X, "Foo")),
                        (position, parameters) -> just(new TestContent(X, "Bar"))
                )
        );
        // when
        final Observable<Content> result =  contentProvider.getContent(X, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Bar"));
    }

    @Test
    public void shouldSelectFirstNotEmpty() {
        // given
        final ContentProvider contentProvider = fetchFirst(of(
                (position, parameters) -> just(new TestContent(X, "")),
                (position, parameters) -> just(new TestContent(X, "Hello World"))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().toIterable().iterator();
        assertThat(content.next().getBody(), is("Hello World"));
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleEmptyContents() {
        // given
        final ContentProvider contentProvider = fetchFirst(of(
                (position, parameters) -> just(new TestContent(X, "")),
                (position, parameters) -> just(new TestContent(X, ""))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().getIterator();
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final ContentProvider contentProvider = fetchFirst(of(
                (position, parameters) -> {
                    throw new IllegalStateException("Bumm!!!");
                },
                (position, parameters) -> just(new TestContent(X, "Yeah!"))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldExecuteStepMultipleTimes() {
        // given
        final ContentProvider nestedProvider = mock(ContentProvider.class);
        when(nestedProvider.getContent(X, emptyParameters())).thenReturn(just(new TestContent(X, "Foo")));
        // and
        final ContentProvider fetchFirstProvider = fetchFirst(of(
                nestedProvider,
                (position, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        fetchFirstProvider.getContent(X, emptyParameters()).toBlocking().single();
        fetchFirstProvider.getContent(X, emptyParameters()).toBlocking().single();
        final Content content = fetchFirstProvider.getContent(X, emptyParameters()).toBlocking().single();
        // then
        assertThat(content.getBody(), is("Foo"));
        verify(nestedProvider, times(3)).getContent(X, emptyParameters());
    }

    private static final class TestContent extends SingleContent {
        private final String text;
        private AbcPosition position;

        TestContent(final AbcPosition position,
                    final String text) {
            this.position = position;
            this.text = text;
        }

        @Override
        public String getSource() {
            return text;
        }

        @Override
        public Position getPosition() {
            return position;
        }

        @Override
        public boolean isAvailable() {
            return !text.isEmpty();
        }

        @Override
        public String getBody() {
            return text;
        }

        @Override
        public Headers getHeaders() {
            return emptyHeaders();
        }

        @Override
        public LocalDateTime getCreated() {
            return now();
        }

    }
}