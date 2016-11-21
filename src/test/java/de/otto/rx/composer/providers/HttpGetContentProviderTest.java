package de.otto.rx.composer.providers;

import com.damnhandy.uri.template.UriTemplate;
import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.http.HttpClient;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Iterator;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Content.Availability.*;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchViaHttpGet;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.*;

public class HttpGetContentProviderTest {

    @Test
    public void shouldFetchContentByUrl() throws Exception {
        // given
        final Response response = someResponse(200, "Foo");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = fetchViaHttpGet(mockClient, "/test", TEXT_PLAIN_TYPE);
        final Content content = contentProvider.getContent(X, emptyParameters()).toBlocking().single();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.getAvailability(), is(AVAILABLE));
        assertThat(content.getBody(), is("Foo"));
    }

    @Test
    public void shouldFetchContentByUriTemplate() {
        // given
        final Response response = someResponse(200, "FooBar");
        final HttpClient mockClient = someHttpClient(response, "/test?foo=bar");
        // when
        final ContentProvider contentProvider = fetchViaHttpGet(mockClient, fromTemplate("/test{?foo}"), TEXT_PLAIN_TYPE);
        final Content content = contentProvider.getContent(X, parameters(of("foo", "bar"))).toBlocking().single();
        // then
        verify(mockClient).get("/test?foo=bar", TEXT_PLAIN_TYPE);
        assertThat(content.getAvailability(), is(AVAILABLE));
        assertThat(content.getBody(), is("FooBar"));
    }

    @Test
    public void shouldIgnoreEmptyContent() {
        // given
        final Response response = someResponse(200, "");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = fetchViaHttpGet(mockClient, "/test", TEXT_PLAIN_TYPE);
        final Iterator<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking().getIterator();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleHttpErrors() {
        // given
        final Response response = someResponse(500, "Some Error Description");
        final HttpClient mockClient = someHttpClient(response, "/test");
        // when
        final ContentProvider contentProvider = fetchViaHttpGet(mockClient, "/test", TEXT_PLAIN_TYPE);
        final Iterator<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking().getIterator();
        // then
        verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.hasNext(), is(false));
    }

    @Test
    public void shouldHandleExceptions() {
        // given
        // this will throw an Exception (from Jersey Client) because /test is not an absolute URL.
        final HttpClient httpClient = new HttpClient(1, 1);
        // when
        final ContentProvider contentProvider = fetchViaHttpGet(httpClient, "/test", TEXT_PLAIN_TYPE);
        final Iterator<Content> content = contentProvider.getContent(X, emptyParameters()).toBlocking().getIterator();
        // then
        //verify(mockClient).get("/test", TEXT_PLAIN_TYPE);
        assertThat(content.hasNext(), is(false));
    }

    private Response someResponse(final int status, final String body) {
        final Response response = mock(Response.class);
        when(response.readEntity(String.class)).thenReturn(body);
        when(response.getStatus()).thenReturn(status);
        return response;
    }

    private HttpClient someHttpClient(final Response response, final String uri) {
        final HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.get(uri, TEXT_PLAIN_TYPE)).thenReturn(just(response));
        return mockClient;
    }
}