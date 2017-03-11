package de.otto.rx.composer.content;

import org.glassfish.jersey.message.internal.Statuses;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static de.otto.rx.composer.content.AbcPosition.E;
import static de.otto.rx.composer.content.ErrorContent.ErrorSource.CLIENT_ERROR;
import static de.otto.rx.composer.content.ErrorContent.ErrorSource.EXCEPTION;
import static de.otto.rx.composer.content.ErrorContent.ErrorSource.OTHER;
import static de.otto.rx.composer.content.ErrorContent.ErrorSource.SERVER_ERROR;
import static de.otto.rx.composer.content.ErrorContent.errorContent;
import static de.otto.rx.composer.content.ErrorContent.httpErrorContent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErrorContentTest {

    @Test
    public void shouldBeErrorContent() {
        final ErrorContent ec = errorContent(E, new IllegalStateException("Bumm!"), 0);
        assertThat(ec.isErrorContent(), is(true));
        assertThat(ec.asErrorContent(), is(ec));
    }

    @Test
    public void shouldCreateExceptionErrorContent() {
        final ErrorContent ec = errorContent(E, new IllegalStateException("Bumm!"), 0);
        assertThat(ec.getErrorReason(), is("Bumm!"));
        assertThat(ec.getErrorSource(), is(EXCEPTION));
        assertThat(ec.getThrowable().isPresent(), is(true));
    }

    @Test
    public void shouldCreateOtherErrorContent() {
        final ErrorContent ec = errorContent("test", E, "some error", 0);
        assertThat(ec.getErrorReason(), is("some error"));
        assertThat(ec.getErrorSource(), is(OTHER));
        assertThat(ec.getThrowable().isPresent(), is(false));
    }

    @Test
    public void shouldCreateClientErrorContent() {
        final Response response = mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Statuses.from(404));
        final ErrorContent ec = httpErrorContent("test", E, response, 0);
        assertThat(ec.getErrorReason(), is("Not Found"));
        assertThat(ec.getErrorSource(), is(CLIENT_ERROR));
        assertThat(ec.getThrowable().isPresent(), is(false));
    }

    @Test
    public void shouldCreateServerErrorContent() {
        final Response response = mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Statuses.from(503));
        final ErrorContent ec = httpErrorContent("test", E, response, 0);
        assertThat(ec.getErrorReason(), is("Service Unavailable"));
        assertThat(ec.getErrorSource(), is(SERVER_ERROR));
        assertThat(ec.getThrowable().isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentException() {
        final Response response = mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Statuses.from(200));
        httpErrorContent("test", E, response, 0);
    }
}