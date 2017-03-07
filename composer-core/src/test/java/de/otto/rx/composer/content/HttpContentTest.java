package de.otto.rx.composer.content;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static de.otto.rx.composer.content.AbcPosition.A;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpContentTest {

    @Test
    public void shouldPropagateResponseHeadersToContent() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.getStringHeaders()).thenReturn(new StringKeyIgnoreCaseMultivaluedMap<String>() {{
            put("X-SomeHeader", singletonList("somevalue"));
            put("x-otherheader", asList("foo", "bar"));
        }});
        // when
        final HttpContent content = new HttpContent("http://example.com/test", A, mockResponse, 0L);
        // then
        assertThat(content.getHeaders().getAll("x-otherheader"), contains("foo", "bar"));
    }

    @Test
    public void shouldGetAvailableContentWithBody() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.readEntity(String.class)).thenReturn("Hello Test");
        // when
        final HttpContent content = new HttpContent("http://example.com/test", A, mockResponse, 0L);
        // then
        assertThat(content.isAvailable(), is(true));
        assertThat(content.getBody(), is("Hello Test"));
    }

    @Test
    public void shouldBeUnavailableOnEmptyBody() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.readEntity(String.class)).thenReturn("");
        when(mockResponse.getStatus()).thenReturn(200);
        // when
        final HttpContent content = new HttpContent("http://example.com/test", A, mockResponse, 0L);
        // then
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));
    }

    @Test
    public void shouldBeUnavailableOnError() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(404);
        // when
        final HttpContent content = new HttpContent("http://example.com/test", A, mockResponse, 0L);
        // then
        assertThat(content.isAvailable(), is(false));
        assertThat(content.getBody(), is(""));
    }
}