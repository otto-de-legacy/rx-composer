package de.otto.edison.aggregator;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.edison.aggregator.content.Contents;
import de.otto.edison.aggregator.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.edison.aggregator.Plan.planIsTo;
import static de.otto.edison.aggregator.content.AbcPosition.X;
import static de.otto.edison.aggregator.content.AbcPosition.Y;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.providers.ContentProviders.httpContent;
import static de.otto.edison.aggregator.steps.Steps.fetch;
import static de.otto.edison.aggregator.steps.Steps.fetchFirstWithContent;
import static de.otto.edison.aggregator.steps.Steps.fetchQuickest;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AggregatorAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

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

        try (final HttpClient httpClient = new HttpClient()) {
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
            assertThat(result.getContent(X).get().getContent(), is("Hello"));
            assertThat(result.getContent(Y).get().getContent(), is("World"));
        }
    }

    @Test
    public void shouldSelectFirstFromMultipleContents() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(100, TimeUnit.MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient()) {
            final Plan plan = planIsTo(
                    fetchFirstWithContent(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getContent(), is("Hello"));
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

        try (final HttpClient httpClient = new HttpClient()) {
            final Plan plan = planIsTo(
                    fetchFirstWithContent(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getContent(), is("World"));
        }
    }

    @Test
    public void shouldSelectQuickest() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(1, TimeUnit.SECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient()) {
            final Plan plan = planIsTo(
                    fetchQuickest(X, of(
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(1));
            assertThat(result.getContent(X).get().getContent(), is("World"));
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

        try (final HttpClient httpClient = new HttpClient()) {
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


        try (final HttpClient httpClient = new HttpClient()) {
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