package de.otto.rx.composer.client;

import org.junit.Test;

import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static de.otto.rx.composer.client.ServiceClients.serviceClients;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServiceClientsTest {

    @Test
    public void shouldConfigureDefaultServiceClient() {
        ServiceClients serviceClients = serviceClients(noResiliency());

        ServiceClient serviceClient = serviceClients.getDefault();
        assertThat(serviceClient.getClientConfig().isResilient(), is(false));
    }

    @Test
    public void shouldConfigureMoreServiceClients() {
        ServiceClients serviceClients = serviceClients(noResiliency(), singleRetry());

        ServiceClient serviceClient = serviceClients.getBy("default-single-retry");
        assertThat(serviceClient.getClientConfig().isResilient(), is(true));
        assertThat(serviceClient.getClientConfig().getRetries(), is(1));
    }

}
