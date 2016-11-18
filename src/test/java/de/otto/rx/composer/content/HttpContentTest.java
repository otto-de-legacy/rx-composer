package de.otto.rx.composer.content;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Content.Availability.AVAILABLE;
import static de.otto.rx.composer.content.Content.Availability.EMPTY;
import static de.otto.rx.composer.content.Content.Availability.ERROR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpContentTest {

    @Test
    public void shouldIgnoreCaseOfKeys() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.getHeaders()).thenReturn(new StringKeyIgnoreCaseMultivaluedMap<Object>() {{
            put("X-SomeHeader", singletonList("somevalue"));
        }});
        // when
        final HttpContent content = new HttpContent(A, mockResponse);
        // then
        assertThat(content.getHeaders().getValue("x-someheader", String.class).get(), is("somevalue"));
    }

    @Test
    public void shouldPropagateResponseHeadersToContent() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.getHeaders()).thenReturn(new StringKeyIgnoreCaseMultivaluedMap<Object>() {{
            put("X-SomeHeader", singletonList("somevalue"));
            put("x-otherheader", asList("foo", "bar"));
        }});
        // when
        final HttpContent content = new HttpContent(A, mockResponse);
        // then
        assertThat(content.getHeaders().getValues("x-otherheader", String.class), contains("foo", "bar"));
    }

    @Test
    public void shouldGetAvailableContentWithBody() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.readEntity(String.class)).thenReturn("Hello Test");
        // when
        final HttpContent content = new HttpContent(A, mockResponse);
        // then
        assertThat(content.getBody(), is("Hello Test"));
        assertThat(content.getAvailability(), is(AVAILABLE));
    }

    @Test
    public void shouldBeEmptyIfNoContentIsReturned() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.readEntity(String.class)).thenReturn("");
        when(mockResponse.getStatus()).thenReturn(200);
        // when
        final HttpContent content = new HttpContent(A, mockResponse);
        // then
        assertThat(content.getBody(), is(""));
        assertThat(content.getAvailability(), is(EMPTY));
    }

    @Test
    public void shouldBeErrorContent() {
        // given
        final Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(404);
        // when
        final HttpContent content = new HttpContent(A, mockResponse);
        // then
        assertThat(content.getBody(), is(""));
        assertThat(content.getAvailability(), is(ERROR));
    }
}