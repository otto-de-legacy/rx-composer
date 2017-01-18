package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.http.HttpClient;
import de.otto.rx.composer.page.Page;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.withSingle;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class HttpFragmentsAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Test
    public void shouldExecutePlanWithTwoRestResources() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(200));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN))
                    ),
                    fragment(
                            Y,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            );

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(2));
            assertThat(result.get(X).getBody(), is("Hello"));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

    @Test
    public void shouldExecutePlanWithEmptyContent() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("", "text/plain").withStatus(200));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN))
                    ),
                    fragment(
                            Y,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            );

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

    @Test
    public void shouldExecutePlanWithHttpErrors() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someErrorContent").withMethod(GET),
                giveResponse("Server Error", "text/plain").withStatus(500));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someErrorContent", TEXT_PLAIN))
                    ),
                    fragment(
                            Y,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            );

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

    @Test
    public void shouldExecutePlanWithExceptions() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            withSingle(contentFrom(httpClient, "INVALID_URL", TEXT_PLAIN))
                    ),
                    fragment(
                            Y,
                            withSingle(contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            );

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

}