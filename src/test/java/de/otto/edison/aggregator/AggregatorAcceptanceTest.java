package de.otto.edison.aggregator;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.edison.aggregator.http.HttpClient;
import org.junit.Rule;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static de.otto.edison.aggregator.AggregatorAcceptanceTest.Test.FIRST;
import static de.otto.edison.aggregator.AggregatorAcceptanceTest.Test.SECOND;
import static de.otto.edison.aggregator.HttpContentProvider.httpContent;
import static de.otto.edison.aggregator.Parameters.emptyParameters;
import static de.otto.edison.aggregator.Plan.planIsTo;
import static de.otto.edison.aggregator.Steps.fetch;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AggregatorAcceptanceTest {

    /*
    @Test
    public void shouldCreatePlanToFetchOneResource() {
        // given
        final Plan plan = planIsTo(
                fetch(
                        textResource("Hello")
                )
        );
        // when
        final ImmutableList<Content> contents = plan.execute();
        // then
        assertThat(contents.getContent(0).getContent(), is("Hello"));
    }
    */

    /*
    @Test
    public void shouldCreatePlanToFetchMultipleResources() {
        // given
        final Plan plan = planIsTo(
                fetch(
                        textResource("Hello")
                ),
                fetch(
                        textResource("World")
                )
        );
        // when
        final ImmutableList<String> contents = plan.execute()
                .stream()
                .map(Content::getContent)
                .collect(toImmutableList());
        // then
        assertThat(contents, contains("Hello", "World"));
    }
    */

    /*
    @Test
    public void shouldProvideInputParametersToSteps() {
        // given
        final Parameters someParameters = from(ImmutableMap.of(
                "message", "World"
        ));
        final Plan plan = planIsTo(
                        fetch(paramTextResource("Hello %s")),
                        fetch(paramTextResource("Hi %s"))
        );
        // when
        final ImmutableList<String> contents = plan.execute(someParameters)
                .stream()
                .map(Content::getContent)
                .collect(toImmutableList());
        // then
        assertThat(contents, contains("Hello World", "Hi World"));
    }
    */

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    enum Test implements Position {FIRST, SECOND}

    @org.junit.Test
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
                            FIRST,
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    fetch(
                            SECOND,
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getContents(), hasSize(2));
            assertThat(result.getContent(FIRST).get().getContent(), is("Hello"));
            assertThat(result.getContent(SECOND).get().getContent(), is("World"));
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
                            FIRST,
                            httpContent(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE)
                    ),
                    fetch(
                            SECOND,
                            httpContent(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE)
                    )
            );

            final Contents result = plan.execute(emptyParameters());

            assertThat(result.getContents(), hasSize(1));
            assertThat(result.hasErrors(), is(true));
            assertThat(result.getErrors(), contains(FIRST));
        }
    }

}