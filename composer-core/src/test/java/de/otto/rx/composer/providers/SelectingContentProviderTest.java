package de.otto.rx.composer.providers;

import de.otto.rx.composer.content.*;
import de.otto.rx.composer.tracer.Tracer;
import org.junit.Test;
import rx.Observable;

import java.time.LocalDateTime;
import java.util.Iterator;

import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.withAll;
import static de.otto.rx.composer.providers.ContentProviders.withFirst;
import static de.otto.rx.composer.providers.ContentProviders.withFirstMatching;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

public class SelectingContentProviderTest {

    @Test
    public void shouldFetchFirstWithContent() {
        // given
        final ContentProvider contentProvider = withFirst(of(
                (position, context, parameters) -> just(new TestContent(X, "Foo")),
                (position, context, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        final Observable<Content> result =  contentProvider.getContent(X, new Tracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldFetchAllWithContent() {
        // given
        final Tracer tracer = new Tracer();
        final ContentProvider contentProvider = withAll(of(
                (position, context, parameters) -> just(new TestContent(X, "Foo")),
                (position, context, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        final Observable<Content> result =  contentProvider.getContent(X, tracer, emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.isComposite(), is(true));
        assertThat(content.getBody(), is("Foo\nBar"));
    }

    @Test
    public void shouldFetchFirstMatchingPredicate() {
        // given
        final ContentProvider contentProvider = withFirstMatching(
                (content) -> content.getBody().contains("a"),
                of(
                        (position, context, parameters) -> just(new TestContent(X, "Foo")),
                        (position, context, parameters) -> just(new TestContent(X, "Bar"))
                )
        );
        // when
        final Observable<Content> result =  contentProvider.getContent(X, new Tracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Bar"));
    }

    @Test
    public void shouldSelectFirstNotEmpty() {
        // given
        final ContentProvider contentProvider = withFirst(of(
                (position, context, parameters) -> just(new TestContent(X, "")),
                (position, context, parameters) -> just(new TestContent(X, "Hello World"))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, new Tracer(), emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().toIterable().iterator();
        assertThat(content.next().getBody(), is("Hello World"));
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleEmptyContents() {
        // given
        final ContentProvider contentProvider = withFirst(of(
                (position, context, parameters) -> just(new TestContent(X, "")),
                (position, context, parameters) -> just(new TestContent(X, ""))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, new Tracer(), emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().getIterator();
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final ContentProvider contentProvider = withFirst(of(
                someContentProviderThrowing(new IllegalStateException("Bumm!!!")),
                (position, context, parameters) -> just(new TestContent(X, "Yeah!"))
        ));
        // when
        final Observable<Content> result = contentProvider.getContent(X, new Tracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldExecuteFragmentMultipleTimes() {
        // given
        final Tracer context = new Tracer();
        final ContentProvider nestedProvider = mock(ContentProvider.class);
        when(nestedProvider.getContent(X, context, emptyParameters())).thenReturn(just(new TestContent(X, "Foo")));
        // and
        final ContentProvider fetchFirstProvider = withFirst(of(
                nestedProvider,
                (position, requestContext, parameters) -> just(new TestContent(X, "Bar"))
        ));
        // when
        fetchFirstProvider.getContent(X, context, emptyParameters()).toBlocking().single();
        fetchFirstProvider.getContent(X, context, emptyParameters()).toBlocking().single();
        final Content content = fetchFirstProvider.getContent(X, context, emptyParameters()).toBlocking().single();
        // then
        assertThat(content.getBody(), is("Foo"));
        verify(nestedProvider, times(3)).getContent(X, context, emptyParameters());
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

    private ContentProvider someContentProviderThrowing(final Exception e) {
        final ContentProvider delegate = mock(ContentProvider.class);
        when(delegate.getContent(any(Position.class), any(Tracer.class), any(Parameters.class))).thenReturn(fromCallable(() -> {
            throw e;
        }));
        return delegate;
    }

}