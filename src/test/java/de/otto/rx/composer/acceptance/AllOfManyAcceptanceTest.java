package de.otto.rx.composer.acceptance;

import com.github.restdriver.clientdriver.ClientDriverRule;
import de.otto.rx.composer.client.ServiceClient;
import de.otto.rx.composer.content.CompositeContent;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.page.Page;
import org.junit.Rule;
import org.junit.Test;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static com.google.common.collect.ImmutableList.of;
import static de.otto.rx.composer.client.HttpServiceClient.noRetriesClient;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.withAll;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
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

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withAll(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
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

        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withAll(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));

            final Contents result = page.fetchWith(emptyParameters());
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
        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withAll(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));
            final Contents result = page.fetchWith(emptyParameters());
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
        try (final ServiceClient serviceClient = noRetriesClient()) {
            final Page page = consistsOf(
                    fragment(X, withAll(of(
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN),
                            contentFrom(serviceClient, driver.getBaseUrl() + "/someOtherContent", TEXT_PLAIN))
                    )
            ));
            final Contents result = page.fetchWith(emptyParameters());
            assertThat(result.getAll(), hasSize(1));
            assertThat(result.get(X).isComposite(), is(true));
            final CompositeContent content = result.get(X).asComposite();
            assertThat(content.getContents(), hasSize(2));
            assertThat(content.getBody(), is("World\nWorld"));
        }
    }

}