package de.otto.edison.aggregator.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.collect.ImmutableMap;
import de.otto.edison.aggregator.Plan;
import de.otto.edison.aggregator.content.Content;
import de.otto.edison.aggregator.content.Contents;
import de.otto.edison.aggregator.content.Parameters;
import de.otto.edison.aggregator.content.Position;
import de.otto.edison.aggregator.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.edison.aggregator.Plan.planIsTo;
import static de.otto.edison.aggregator.acceptance.AggregatorAcceptanceTest.ShowCaseContent.*;
import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.AbcPosition.Y;
import static de.otto.edison.aggregator.content.AbcPosition.Z;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.content.Parameters.parameters;
import static de.otto.edison.aggregator.providers.ContentProviders.httpContent;
import static de.otto.edison.aggregator.steps.Steps.fetch;
import static de.otto.edison.aggregator.steps.Steps.fetchFirst;
import static de.otto.edison.aggregator.steps.Steps.fetchQuickest;
import static de.otto.edison.aggregator.steps.Steps.then;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AggregatorAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    enum ShowCaseContent implements Position {
        TOPMOST_BANNER,
        SLIDESHOW_TEASER,
        SHOPTEASER,
        PRODUCT_RECO,
        SERVICE_PROMOTIONS,
        BRAND_CINEMA
    }

    //@Test
    public void showcase() {
        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            // The Plan used to fetch and select contents from different services.
            final Plan plan = planIsTo(
                    fetchFirst(
                            TOPMOST_BANNER, of(
                                    httpContent(httpClient, driver.getBaseUrl() + "/imageBanner", TEXT_HTML_TYPE),
                                    httpContent(httpClient, driver.getBaseUrl() + "/campaign", TEXT_HTML_TYPE)
                            )
                    ),
                    fetch(
                            SLIDESHOW_TEASER,
                            httpContent(httpClient, driver.getBaseUrl() + "/slideshowteaser", TEXT_HTML_TYPE)
                    ),
                    fetch(
                            SHOPTEASER,
                            httpContent(httpClient, driver.getBaseUrl() + "/shopteaser", TEXT_PLAIN_TYPE)
                    ),
                    fetchFirst(
                            PRODUCT_RECO, of(
                                httpContent(httpClient, driver.getBaseUrl() + "/personalreco", TEXT_PLAIN_TYPE),
                                httpContent(httpClient, driver.getBaseUrl() + "/pzk", TEXT_PLAIN_TYPE),
                                httpContent(httpClient, driver.getBaseUrl() + "/topseller", TEXT_PLAIN_TYPE)
                            )
                    ),
                    fetch(
                            SERVICE_PROMOTIONS,
                            httpContent(httpClient, driver.getBaseUrl() + "/servicepromos", TEXT_PLAIN_TYPE)
                    ),
                    fetch(
                            BRAND_CINEMA,
                            httpContent(httpClient, driver.getBaseUrl() + "/brandcinema", TEXT_PLAIN_TYPE)
                    )
            );

            // fetch + select the contents - in parallel. Parameters are propagated to the services.
            final Contents contents = plan.execute(parameters(ImmutableMap.of(
                    "VISITOR", 4711,
                    "BRAND", "Miele",
                    "TESLA_INSIGHTS", "Some Insights from Tesla we would like to propagate to the services."
            )));

            // now we can render the contents as HTML

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldCallPlanWithTwoRestResources() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(200))
                .anyTimes();
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200))
                .anyTimes();

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetch(
                            X,
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    fetch(
                            Y,
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(2));
            assertThat(result.getContent(X).get().getBody(), is("Hello"));
            assertThat(result.getContent(Y).get().getBody(), is("World"));
        }
    }

    @Test
    public void shouldHandleNestedSteps() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(200));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withParam("param", "Hello").withMethod(GET),
                giveResponse("World", "text/plain").withStatus(200));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("Edison", "text/plain").withStatus(200));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetch(
                            X,
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            then(
                                    (final Content content) -> Parameters.from(ImmutableMap.of("param", content.getBody())),
                                    fetch(
                                            Y,
                                            httpContent(httpClient, fromTemplate(driver.getBaseUrl() + "/someOtherContent{?param}"), TEXT_PLAIN_TYPE)
                                    ),
                                    fetch(
                                            Z,
                                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                                    )
                            )
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(3));
            assertThat(result.getContent(X).get().getBody(), is("Hello"));
            assertThat(result.getContent(Y).get().getBody(), is("World"));
            assertThat(result.getContent(Z).get().getBody(), is("Edison"));
        }
    }

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
            final Plan plan = planIsTo(
                    fetchFirst(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getBody(), is("Hello"));
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
            final Plan plan = planIsTo(
                    fetchFirst(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );
            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getBody(), is("World"));
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
            final Plan plan = planIsTo(
                    fetchFirst(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getBody(), is("World"));
        }
    }

    @Test
    public void shouldSelectQuickest() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(50, MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetchQuickest(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getBody(), is("World"));
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

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetchQuickest(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getBody(), is("World"));
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

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetchQuickest(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(0));
        }
    }

    @org.junit.Test
    public void shouldIgnoreHttpResponseErrors() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("", "text/plain")
                        .withStatus(404));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"))
                .anyTimes();


        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    fetch(
                            X,
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    fetch(
                            Y,
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());

            assertThat(result.getContents(), hasSize(1));
            assertThat(result.hasErrors(), is(true));
            assertThat(result.getErrors(), contains(X));
        }
    }

}