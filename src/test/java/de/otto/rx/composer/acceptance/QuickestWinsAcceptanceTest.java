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
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchQuickest;
import static de.otto.rx.composer.providers.ContentProviders.fetchViaHttpGet;
import static de.otto.rx.composer.steps.Steps.forPos;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class QuickestWinsAcceptanceTest {

    @Rule
    public ClientDriverRule driver = new ClientDriverRule();

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
                    forPos(X, fetchQuickest(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));

            final Contents result = plan.execute(emptyParameters());
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

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchQuickest(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));

            final Contents result = plan.execute(emptyParameters());
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

        try (final HttpClient httpClient = new HttpClient(1000, 1000)) {
            final Plan plan = planIsTo(
                    forPos(X, fetchQuickest(of(
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN_TYPE),
                            fetchViaHttpGet(httpClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN_TYPE))
                    )
            ));

            final Contents result = plan.execute(emptyParameters());
            assertThat(result.getAll(), hasSize(0));
        }
    }

}