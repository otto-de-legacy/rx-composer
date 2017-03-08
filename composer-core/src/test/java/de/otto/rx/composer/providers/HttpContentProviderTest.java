package de.otto.rx.composer.providers;

import de.otto.rx.composer.client.ClientConfig;
import de.otto.rx.composer.client.HttpServiceClient;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Content;
import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Test;
import rx.observables.BlockingObservable;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import java.util.Iterator;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

public class HttpContentProviderTest {

    @Test
    public void shouldFetchContentByUrl() throws Exception {
        // given
        final Response response = someResponse(200, "Foo");
        final ServiceClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        final Content content = contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking().single();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldFetchContentByUriTemplate() {
        // given
        final Response response = someResponse(200, "FooBar");
        final ServiceClient mockClient = someHttpClient(response, "/test?foo=bar");
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, fromTemplate("/test{?foo}"), TEXT_PLAIN);
        final Content content = contentProvider.getContent(X, noOpTracer(), parameters(of("foo", "bar"))).toBlocking().single();
        // then
        verify(mockClient).get("/test?foo=bar", TEXT_PLAIN_TYPE);
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("FooBar"));
    }

    @Test
    public void shouldIgnoreEmptyContent() {
        // given
        final Response response = someResponse(200, "");
        final ServiceClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        final Iterator<Content> content = contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking().getIterator();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.hasNext(), is(false));
    }

    @Test(expected = ServerErrorException.class)
    public void shouldThrowExceptionOnHttpServerError() {
        // given
        final Response response = someResponse(500, "Some Server Error");
        final ServiceClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        final BlockingObservable<Content> content = contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        content.single();
    }

    @Test
    public void shouldReturnEmptyContentOnHttpClientError() {
        // given
        final Response response = someResponse(404, "Not Found");
        final ServiceClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        final BlockingObservable<Content> content = contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.getIterator().hasNext(), is(false));
    }

    @Test(expected = RuntimeException.class)
    public void shouldPassExceptions() {
        // given
        // this will throw an Exception (from Jersey Client) because /test is not an absolute URL.
        final ServiceClient mockClient = someHttpClient(mock(Response.class), "/test");
        when(mockClient.get("/test", TEXT_PLAIN_TYPE)).thenReturn(fromCallable(() -> {
            throw new RuntimeException("KA-WUMMMM!");
        }));
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking().single();
    }

    @Test(expected = RuntimeException.class)
    public void shouldPassCheckedExceptionAsRuntimeException() {
        // given
        // this will throw an Exception (from Jersey Client) because /test is not an absolute URL.
        final ServiceClient mockClient = someHttpClient(mock(Response.class), "/test");
        when(mockClient.get("/test", TEXT_PLAIN_TYPE)).thenReturn(fromCallable(() -> {
            throw new Exception("KA-WUMMMM!");
        }));
        // when
        final ContentProvider contentProvider = contentFrom(mockClient, "/test", TEXT_PLAIN);
        contentProvider.getContent(X, noOpTracer(), emptyParameters()).toBlocking().single();
    }

    private Response someResponse(final int status, final String body) {
        final Response response = mock(Response.class);
        when(response.readEntity(String.class)).thenReturn(body);
        when(response.getStatus()).thenReturn(status);
        when(response.getStatusInfo()).thenReturn(Statuses.from(status));
        return response;
    }

    private ServiceClient someHttpClient(final Response response, final String uri) {
        final HttpServiceClient mockClient = mock(HttpServiceClient.class);
        when(mockClient.get(uri, TEXT_PLAIN_TYPE)).thenReturn(just(response));
        when(mockClient.getClientConfig()).thenReturn(ClientConfig.noResiliency());
        return mockClient;
    }
}