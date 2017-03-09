package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.client.ServiceClients;
import de.otto.rx.composer.content.*;
import de.otto.rx.composer.page.Page;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static de.otto.rx.composer.client.DefaultRef.noRetries;
import static de.otto.rx.composer.client.DefaultRef.singleRetry;
import static de.otto.rx.composer.client.HttpServiceClient.noResiliencyClient;
import static de.otto.rx.composer.client.HttpServiceClient.noRetriesClient;
import static de.otto.rx.composer.client.HttpServiceClient.singleRetryClient;
import static de.otto.rx.composer.client.ServiceClients.defaultClients;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.StaticTextContent.staticTextContent;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.fallbackTo;
import static de.otto.rx.composer.providers.ContentProviders.withSingle;
import static de.otto.rx.composer.tracer.NoOpTracer.noOpTracer;
import static de.otto.rx.composer.tracer.TracerBuilder.loggingStatisticsTracer;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static rx.Observable.just;

public class ResilientHttpFragmentsAcceptanceTest {

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
    public void shouldExecutePlanWithTwoRestResources() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(200));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN)
                    ),
                    fragment(
                            Y,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
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

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN)
                    ),
                    fragment(
                            Y,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
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

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someErrorContent", TEXT_PLAIN)
                    ),
                    fragment(
                            Y,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

    @Test
    public void shouldRetryAfterHttpErrors() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someFlakyContent").withMethod(GET),
                giveResponse("Server Error", "text/plain")
                        .withStatus(500)).times(1);
        driver.addExpectation(
                onRequestTo("/someFlakyContent").withMethod(GET),
                giveResponse("Some Content", "text/plain")
                        .withStatus(200)).times(1);

        try (final ServiceClient serviceClient = singleRetryClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someFlakyContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(true));
            assertThat(result.get(X).getBody(), is("Some Content"));
        }
    }

    @Test
    public void shouldFailAfterRetriesWithHttpErrors() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someFailingContent").withMethod(GET),
                giveResponse("First Server Error", "text/plain")
                        .withStatus(500)).times(2);

        try (final ServiceClient serviceClient = singleRetryClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someFailingContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(0));
            assertThat(result.get(X).isAvailable(), is(false));
        }
    }

    @Test
    public void shouldFallbackOnHttpError() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someErrorContent").withMethod(GET),
                giveResponse("Server Error", "text/plain").withStatus(500)).times(2);
        driver.addExpectation(
                onRequestTo("/someFallbackContent").withMethod(GET),
                giveResponse("Fallback Content", "text/plain").withStatus(200));

        try (final ServiceClients clients = defaultClients()) {
            final Page page = consistsOf(
                    fragment(X,
                            withSingle(
                                    contentFrom(clients.getBy(singleRetry), driver.getBaseUrl() + "/someErrorContent", TEXT_PLAIN,
                                            fallbackTo(contentFrom(clients.getBy(noRetries), driver.getBaseUrl() + "/someFallbackContent", TEXT_PLAIN))
                                    )
                            )
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(true));
            assertThat(result.get(X).getBody(), is("Fallback Content"));
        }
    }

    @Test
    public void shouldFallbackToStaticContent() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someErrorContent").withMethod(GET),
                giveResponse("Server Error", "text/plain").withStatus(500));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X,
                            withSingle(
                                    contentFrom(serviceClient, driver.getBaseUrl() + "/someErrorContent", TEXT_PLAIN,
                                            fallbackTo((position, ctx, parameters) -> just(someContent(position, "Some Fallback")))
                                    )
                            )
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(true));
            assertThat(result.get(X).getBody(), is("Some Fallback"));
        }
    }

    @Test
    public void shouldRetryBeforeFallback() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someFlakyContent").withMethod(GET),
                giveResponse("Server Error", "text/plain").withStatus(500));

        driver.addExpectation(
                onRequestTo("/someFlakyContent").withMethod(GET),
                giveResponse("Some Content", "text/plain").withStatus(200));

        try (final ServiceClient serviceClient = singleRetryClient()) {
            final Page page = consistsOf(
                    fragment(X,
                            withSingle(
                                    contentFrom(serviceClient, driver.getBaseUrl() + "/someFlakyContent", TEXT_PLAIN,
                                            fallbackTo((position, ctx, parameters) -> just(someContent(position, "Some Fallback")))
                                    )
                            )
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(true));
            assertThat(result.get(X).getBody(), is("Some Content"));
        }
    }

    @Test
    public void shouldExecutePlanWithExceptions() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(
                            X,
                            contentFrom(serviceClient, "INVALID_URL", TEXT_PLAIN)
                    ),
                    fragment(
                            Y,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN)
                    )
            );

            final Contents result = page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

    // TODO: check for retries!
    // @Test
    public void shouldOpenCircuitOnServerError() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Some Server Error", "text/plain")
                        .withStatus(500))
                .anyTimes();

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(
                            Y,
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN)
                    )
            );
            for (int i = 0; i < 150; i++) {
                page.fetchWith(emptyParameters(), loggingStatisticsTracer());
            }

            /*assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isAvailable(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
            */
        }
    }

    private Content someContent(final Position position, final String body) {
        return staticTextContent(position.name(), position, body);

    }

}