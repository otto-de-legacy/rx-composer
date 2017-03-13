package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.page.Page;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static de.otto.rx.composer.client.HttpServiceClient.noResiliencyClient;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.htmlBodyOf;
import static de.otto.rx.composer.providers.ContentProviders.withAll;
import static de.otto.rx.composer.providers.ContentProviders.withSingle;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class HtmlBodyAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Before
    public void warmUp() throws Exception {
        driver.addExpectation(
                onRequestTo("/warmup").withMethod(GET),
                giveResponse("ok", "text/plain"));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            contentFrom(serviceClient, driver.getBaseUrl() + "/warmup", TEXT_PLAIN)
                    .getContent(() -> "warmup", noOpTracer(), emptyParameters())
                    .toBlocking()
                    .first();
        }
    }

    @Test
    public void shouldExtractBodyFromHtmlPage() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("<html><body><h1>\nHello World\n</h1></body></html>", "text/html").withStatus(200));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            final Page page = somePage(serviceClient);

            final Contents result = page.fetchWith(emptyParameters(), noOpTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("<h1>\nHello World\n</h1>"));
        }
    }

    @Test
    public void shouldExtractBodyFromHtmlPageWithDoctypeAndExtraAttributes() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("<!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>\nHello World\n</h1></body></html>", "text/html").withStatus(200));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            final Page page = somePage(serviceClient);

            final Contents result = page.fetchWith(emptyParameters(), noOpTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("<h1>\nHello World\n</h1>"));
        }
    }

    @Test
    public void shouldIgnoreMissingBody() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("<h1>Hello World</h1>", "text/html").withStatus(200));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            final Page page = somePage(serviceClient);

            final Contents result = page.fetchWith(emptyParameters(), noOpTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("<h1>Hello World</h1>"));
        }
    }

    @Test
    public void shouldIgnoreErrorContent() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("<!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>Not Found</h1></body></html>", "text/html").withStatus(404));

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            final Page page = somePage(serviceClient);

            final Contents result = page.fetchWith(emptyParameters(), noOpTracer());
            assertThat(result.getAll(), hasSize(0));
            assertThat(result.get(X).getBody(), is(""));
        }
    }

    @Test
    public void shouldExtractBodyFromHtmlPageForCompositeContents2() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("<!DOCTYPE html><html lang=\"de\"><body some=\"attr\"><h1>Hello World</h1></body></html>", "text/html")
                        .withStatus(200))
                        .times(2)
        ;

        try (final ServiceClient serviceClient = noResiliencyClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            htmlBodyOf(
                                withAll(
                                        contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_HTML),
                                        contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_HTML)
                                )
                            )
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), noOpTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).getBody(), is("<h1>Hello World</h1><h1>Hello World</h1>"));
        }
    }

    private Page somePage(final ServiceClient serviceClient) {
        return consistsOf(
                fragment(X, withSingle(
                        htmlBodyOf(
                                contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_HTML)
                        )
                ))
        );
    }

}