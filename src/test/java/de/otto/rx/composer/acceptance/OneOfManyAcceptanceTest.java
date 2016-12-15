package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.*;
import static de.otto.rx.composer.providers.ContentProviders.withFirst;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class OneOfManyAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Test
    public void shouldSelectFirstFromMultipleContents() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(50, MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(X, withFirst(of(
                            contentFrom(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("Hello"));
        }
    }

    @Test
    public void shouldHandleTimeouts() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(300, MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));
        try (final HttpClient httpClient = new HttpClient(1000, 200)) {
            final Page page = consistsOf(
                    fragment(X, withFirst(of(
                            contentFrom(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));
            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("World"));
        }
    }

    @Test
    public void shouldSelectFirstHavingContent() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(404));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Page page = consistsOf(
                    fragment(X, withFirst(of(
                            contentFrom(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.getBody(X), is("World"));
        }
    }


}