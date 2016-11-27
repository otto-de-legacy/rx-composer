package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.Plan;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.Plan.planIsTo;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchViaHttpGet;
import static de.otto.rx.composer.steps.Steps.forPos;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class SingleStepsAcceptanceTest {

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
            final Plan plan = planIsTo(
                    forPos(
                            X,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    forPos(
                            Y,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
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
            final Plan plan = planIsTo(
                    forPos(
                            X,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    forPos(
                            Y,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).hasContent(), is(false));
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
            final Plan plan = planIsTo(
                    forPos(
                            X,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someErrorContent", TEXT_PLAIN_TYPE)
                    ),
                    forPos(
                            Y,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).hasContent(), is(false));
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
            final Plan plan = planIsTo(
                    forPos(
                            X,
                            fetchViaHttpGet(httpClient, "INVALID_URL", TEXT_PLAIN_TYPE)
                    ),
                    forPos(
                            Y,
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).hasContent(), is(false));
            assertThat(result.get(Y).getBody(), is("World"));
        }
    }

}