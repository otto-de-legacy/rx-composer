package de.otto.edison.aggregator.content;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.junit.Test;

import javax.ws.rs.core.Response;

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
        final HttpContent content = new HttpContent(AbcPosition.A, 0, mockResponse);
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
        final HttpContent content = new HttpContent(AbcPosition.A, 0, mockResponse);
        // then
        assertThat(content.getHeaders().getValues("x-otherheader", String.class), contains("foo", "bar"));
    }
}