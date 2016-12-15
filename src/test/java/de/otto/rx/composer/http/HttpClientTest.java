package de.otto.rx.composer.http;

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
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HttpClientTest {
    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Test
    public void shouldReturnContentOberservable() throws Exception {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Response response = httpClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
            assertThat(response.getStatus(), is(200));
            assertThat(response.readEntity(String.class), is("Hello World"));
        }
    }

    @Test(expected = SocketTimeoutException.class)
    public void shouldTimeoutReadWithSocketTimeoutException() throws Throwable {
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello World", "text/plain").after(300, TimeUnit.MILLISECONDS));
        try (final HttpClient httpClient = new HttpClient(1000, 250)) {
            httpClient.get(driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE).toBlocking().single();
        } catch (final ProcessingException e) {
            throw e.getCause();
        }
    }

}