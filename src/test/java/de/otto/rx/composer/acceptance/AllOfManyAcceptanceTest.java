package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.Plan;
import de.otto.rx.composer.content.CompositeContent;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.Plan.planIsTo;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchAll;
import static de.otto.rx.composer.providers.ContentProviders.fetchViaHttpGet;
import static de.otto.rx.composer.steps.Steps.forPos;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AllOfManyAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

    @Test
    public void shouldSelectMultipleContents() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain"));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchAll(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isComposite(), is(true));
            final CompositeContent content = result.get(X).asComposite();
            assertThat(content.getContents(), hasSize(2));
            assertThat(content.getContents().get(0).getBody(), is("Hello"));
            assertThat(content.getContents().get(1).getBody(), is("World"));
            assertThat(content.getBody(), is("Hello\nWorld"));
        }
    }

    @Test
    public void shouldKeepOrderingOfMultipleContents() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").after(50, MILLISECONDS));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchAll(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isComposite(), is(true));
            final CompositeContent content = result.get(X).asComposite();
            assertThat(content.getContents(), hasSize(2));
            assertThat(content.getBody(), is("Hello\nWorld"));
        }
    }

    @Test
    public void shouldNotReturnCompositeIfOnlyOneContentIsAvailable() throws Exception {
        // given
        driver.addExpectation(
                onRequestTo("/someContent").withMethod(GET),
                giveResponse("Hello", "text/plain").withStatus(400));
        driver.addExpectation(
                onRequestTo("/someOtherContent").withMethod(GET),
                giveResponse("World", "text/plain"));
        try (final HttpClient httpClient = new HttpClient(1000, 200)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchAll(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));
            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isComposite(), is(false));
            assertThat(result.get(X).getBody(), is("World"));
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
                giveResponse("World", "text/plain")).anyTimes();
        try (final HttpClient httpClient = new HttpClient(1000, 200)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchAll(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));
            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isComposite(), is(true));
            final CompositeContent content = result.get(X).asComposite();
            assertThat(content.getContents(), hasSize(2));
            assertThat(content.getBody(), is("World\nWorld"));
        }
    }

}