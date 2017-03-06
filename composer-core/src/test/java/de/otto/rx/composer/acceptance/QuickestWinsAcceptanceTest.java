package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.tracer.Tracer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.client.HttpServiceClient.noResiliencyClient;
import static de.otto.rx.composer.client.HttpServiceClient.noRetriesClient;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.withQuickest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class QuickestWinsAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Before
    public void warmUp() throws Exception {
        driver.addExpectation(
                onRequestTo("/warmup").withMethod(GET),
                giveResponse("ok", "text/plain"));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            contentFrom(serviceClient, driver.getBaseUrl() + "/warmup", TEXT_PLAIN)
                    .getContent(() -> "warmup", new Tracer(), emptyParameters())
                    .toBlocking()
                    .first();
        }
    }

    @Test
    public void shouldSelectQuickest() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(100, MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));


        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withQuickest(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("World"));
        }
    }

    @Test
    public void shouldSelectQuickestWithContent() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("", "text/plain"));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").after(50, MILLISECONDS));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withQuickest(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("World"));
        }
    }

    @Test
    public void shouldNotWaitForQuickestIfAllAreFailing() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(404));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(404));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withQuickest(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(0));
        }
    }

}