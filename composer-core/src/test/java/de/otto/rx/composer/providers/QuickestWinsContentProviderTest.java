package de.otto.rx.composer.providers;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import de.otto.rx.composer.tracer.Tracer;
import org.junit.Test;
import rx.Observable;

import java.util.Iterator;

import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.withQuickest;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

public class QuickestWinsContentProviderTest {

    @Test
    public void shouldReturnOnlyNonEmptyContent() {
        // given
        final ContentProvider fetchQuickest = withQuickest(ImmutableList.of(
                (position, ctx, parameters) -> just(new TestContent("First", X, "")),
                (position, ctx, parameters) -> just(new TestContent("Second", X, "Yeah!"))
        ));
        // when
        final Observable<Content> result = fetchQuickest.getContent(X, noOpTracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleOnlyEmptyContents() {
        // given
        final ContentProvider fetchQuickest = withQuickest(ImmutableList.of(
                (position, ctx, parameters) -> just(new TestContent("First", X, "")),
                (position, ctx, parameters) -> just(new TestContent("Second", X, ""))
        ));
        // when
        final Observable<Content> result = fetchQuickest.getContent(X, noOpTracer(), emptyParameters());
        // then
        final Iterator<Content> content = result.toBlocking().getIterator();
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        final ContentProvider fetchQuickest = withQuickest(ImmutableList.of(
                someContentProviderThrowing(new IllegalStateException("Bumm!!!")),
                (position, ctx, parameters) -> just(new TestContent("Second", X, "Yeah!"))
        ));
        // when
        final Observable<Content> result = fetchQuickest.getContent(X, noOpTracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().single();
        assertThat(content.getBody(), is("Yeah!"));
    }

    @Test
    public void shouldHandleFailingProviders() {
        // given
        final ContentProvider fetchQuickest = withQuickest(ImmutableList.of(
                someContentProviderThrowing(new IllegalStateException("Bumm!!!")),
                someContentProviderThrowing(new IllegalStateException("Bumm!!!"))
        ));
        // when
        final Observable<Content> result = fetchQuickest.getContent(X, noOpTracer(), emptyParameters());
        // then
        final Content content = result.toBlocking().singleOrDefault(null);
        assertThat(content, is(nullValue()));
    }

    private static final class TestContent extends SingleContent {
        private final String source;
        private final String text;
        private final AbcPosition position;

        TestContent(final String source,
                    final AbcPosition position,
                    final String text) {
            this.source = source;
            this.position = position;
            this.text = text;
        }

        @Override
        public String getSource() {
            return source;
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
        public long getStartedTs() {
            return 0L;
        }

        @Override
        public long getCompletedTs() {
            return 0L;
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