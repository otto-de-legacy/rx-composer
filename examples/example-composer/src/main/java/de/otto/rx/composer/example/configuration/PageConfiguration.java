package de.otto.rx.composer.example.configuration;

import de.otto.rx.composer.client.Ref;
import de.otto.rx.composer.client.ServiceClients;
import de.otto.rx.composer.content.Position;
import de.otto.rx.composer.page.Page;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.rx.composer.client.ClientConfig.noResiliency;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ClientConfig.singleRetry;
import static de.otto.rx.composer.client.ServiceClients.serviceClients;
import static de.otto.rx.composer.content.AbcPosition.*;
import static de.otto.rx.composer.content.StaticTextContent.staticTextContent;
import static de.otto.rx.composer.example.configuration.PageConfiguration.PagePosition.INTRO;
import static de.otto.rx.composer.example.configuration.PageConfiguration.Services.*;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.fallbackTo;
import static de.otto.rx.composer.providers.ContentProviders.withAll;
import static de.otto.rx.composer.providers.ContentProviders.withSingle;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

@Configuration
public class PageConfiguration {

    enum PagePosition implements Position {
        INTRO
    }

    enum Services implements Ref {
        introService, helloService, somethingElseService, serviceC, someMissingService, someBrokenService
    }

    private ServiceClients clients;

    @PostConstruct
    public void init() {
        clients = serviceClients(
                singleRetry(introService, 5000, 2000),
                singleRetry(helloService, 5000, 500),
                noResiliency(somethingElseService, 5000, 400),
                singleRetry(serviceC, 5000, 1000),
                singleRetry(someMissingService, 5000, 480),
                noRetries(someBrokenService, 5000, 550)
        );
    }

    @PreDestroy
    public void shutdown() throws Exception {
        clients.close();
    }

    @Bean
    public Page page() {
        return consistsOf(
                fragment(INTRO, withSingle(
                        contentFrom(clients.getBy(introService), "http://localhost:8081/intro", TEXT_HTML))
                ),
                fragment(A, withAll(
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello?name=RxComposer"), TEXT_HTML),
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello{?name}"), TEXT_HTML))
                ),
                fragment(B, withSingle(
                        contentFrom(clients.getBy(somethingElseService), "http://localhost:8081/somethingElse", TEXT_HTML))
                ),
                fragment(C, withAll(
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello{?name}"), TEXT_HTML))
                ),
                fragment(D, withSingle(
                        contentFrom(clients.getBy(someMissingService), "http://localhost:8081/somethingMissing", TEXT_HTML))
                ),
                fragment(E, withAll(
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello?name=RxComposer"), TEXT_HTML),
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello?name=Otto"), TEXT_HTML),
                        contentFrom(clients.getBy(helloService), fromTemplate("http://localhost:8081/hello{?name}"), TEXT_HTML))
                ),
                fragment(F, withSingle(
                        contentFrom(clients.getBy(someBrokenService), "http://localhost:8081/somethingBroken", TEXT_HTML,
                                fallbackTo(
                                        staticTextContent("Fallback for http://localhost:8081/somethingBroken", F, "This is a staticTextFallback for /somethingBroken"))))
                )
        );
    }

}
