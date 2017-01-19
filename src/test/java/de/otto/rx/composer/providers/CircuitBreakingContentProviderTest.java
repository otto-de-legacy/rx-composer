package de.otto.rx.composer.providers;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import de.otto.rx.composer.content.*;
import org.junit.Test;
import rx.observables.BlockingObservable;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.Iterator;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.withResilient;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

public class CircuitBreakingContentProviderTest {

    @Test
    public void shouldFetchContent() throws Exception {
        // given
        final ContentProvider delegate = someContentProvider("some body");

        // when
        final ContentProvider contentProvider = withResilient(delegate, 200, commandKey());
        final Content content = contentProvider.getContent(X, emptyParameters()).toBlocking().single();

        // then
        verify(delegate).getContent(any(Position.class), any(Parameters.class));
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("some body"));
    }

    @Test
    public void shouldIgnoreEmptyContent() {
        // given
        final ContentProvider delegate = someContentProvider("");

        // when
        final ContentProvider contentProvider = withResilient(delegate, 200, commandKey());
        final Iterator<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking().getIterator();

        // then
        verify(delegate).getContent(any(Position.class), any(Parameters.class));
        assertThat(content.hasNext(), is(false));
    }

    @Test(expected = HystrixRuntimeException.class)
    public void shouldThrowExceptionOnHttpServerError() {
        // given
        final ContentProvider delegate = someContentProviderThrowing(new ConnectException("KA-WUMMMM!"));

        // when
        final ContentProvider contentProvider = withResilient(delegate, 200, commandKey());
        final BlockingObservable<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking();
        // then
        verify(delegate).getContent(any(Position.class), any(Parameters.class));
        content.single();
    }

    private static int counter = 0;

    private String commandKey() {
        return "test service #" + counter++;
    }
    private ContentProvider someContentProvider(final String body) {
        final ContentProvider delegate = mock(ContentProvider.class);
        when(delegate.getContent(any(Position.class), any(Parameters.class))).thenReturn(just(someContent(body)));
        return delegate;
    }

    private ContentProvider someContentProviderThrowing(final Exception e) {
        final ContentProvider delegate = mock(ContentProvider.class);
        when(delegate.getContent(any(Position.class), any(Parameters.class))).thenReturn(fromCallable(() -> {
            throw e;
        }));
        return delegate;
    }

    private Content someContent(final String body) {
        return new SingleContent() {

            private LocalDateTime now;

            @Override
            public Position getPosition() {
                return A;
            }

            @Override
            public String getSource() {
                return "";
            }

            @Override
            public boolean isAvailable() {
                return !body.isEmpty();
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public Headers getHeaders() {
                return emptyHeaders();
            }

            @Override
            public LocalDateTime getCreated() {
                now = now();
                return now;
            }
        };

    }

}