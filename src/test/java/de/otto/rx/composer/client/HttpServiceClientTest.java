package de.otto.rx.composer.client;

import com.github.restdriver.clientdriver.ClientDriverRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static de.otto.rx.composer.client.HttpServiceClient.noRetriesClient;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HttpServiceClientTest {
    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Test
    public void shouldReturnContentOberservable() throws Exception {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello World", "text/plain"));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Response response = serviceClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
            assertThat(response.getStatus(), is(200));
            assertThat(response.readEntity(String.class), is("Hello World"));
        }
    }

    @Test(expected = SocketTimeoutException.class)
    public void shouldTimeoutReadWithSocketTimeoutException() throws Throwable {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello World", "text/plain").after(300, TimeUnit.MILLISECONDS));
        try (final ServiceClient httpClient = noRetriesClient("test", 1000, 250)) {
            httpClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
        } catch (final ProcessingException e) {
            throw e.getCause();
        }
    }

    @Test
    public void shouldReturnWithClientErrorResponse() throws Throwable {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Not Found", "text/plain")
                        .withStatus(404));
        try (final ServiceClient httpClient = noRetriesClient()) {
            Response response = httpClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
            assertThat(response.getStatus(), is(404));
            assertThat(response.readEntity(String.class), is("Not Found"));
        }
    }

    @Test
    public void shouldReturnWithServerErrorResponse() throws Throwable {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Server Error", "text/plain")
                        .withStatus(500));
        try (final ServiceClient httpClient = noRetriesClient()) {
            Response response = httpClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
            assertThat(response.getStatus(), is(500));
            assertThat(response.readEntity(String.class), is("Server Error"));
        }
    }

}