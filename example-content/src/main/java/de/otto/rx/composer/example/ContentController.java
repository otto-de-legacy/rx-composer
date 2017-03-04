package de.otto.rx.composer.example;

import de.otto.rx.composer.client.Ref;
import de.otto.rx.composer.client.ServiceClients;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.page.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PreDestroy;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ServiceClients.serviceClients;
import static de.otto.rx.composer.content.AbcPosition.*;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.example.ContentController.Services.*;
import static de.otto.rx.composer.page.Fragments.fragment;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.providers.ContentProviders.contentFrom;
import static de.otto.rx.composer.providers.ContentProviders.withAll;
import static de.otto.rx.composer.providers.ContentProviders.withSingle;
import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Fetches two contents from two different "microservices". An optional request-parameter 'name' is forwarded to the
 * 'hello' service, which is returning 'Hello &lt;name&gt;'.
 */
@Controller
public class ContentController {

    enum Services implements Ref {
        serviceA, serviceB, serviceC, serviceD, serviceE, serviceF
    }
    private final ServiceClients clients = serviceClients(
            noRetries(serviceA, 5000, 500),
            noRetries(serviceB, 5000, 400),
            noRetries(serviceC, 5000, 1000),
            noRetries(serviceD, 5000, 480),
            noRetries(serviceE, 5000, 550),
            noRetries(serviceF, 5000, 2000)
    );

    @PreDestroy
    public void shutdown() throws Exception {
        clients.close();
    }

    @RequestMapping("/")
    public ModelAndView getContent(final @RequestParam(defaultValue = "false") boolean debugMode) {
        final Page page = somePage();
        final Contents contents = page.fetchWith(emptyParameters());
        ModelAndView modelAndView = new ModelAndView("content");
        modelAndView.addObject("contents", contents);
        modelAndView.addObject("debugMode", debugMode);
        return modelAndView;
    }

    private Page somePage() {
        return consistsOf(
                fragment(A, withAll(
                        contentFrom(clients.getBy(serviceA), fromTemplate("http://localhost:8080/hello?name=Rx-Composer"), TEXT_HTML),
                        contentFrom(clients.getBy(serviceA), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(B, withSingle(
                        contentFrom(clients.getBy(serviceE), "http://localhost:8080/somethingElse", TEXT_HTML))
                ),
                fragment(C, withAll(
                        contentFrom(clients.getBy(serviceF), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(D, withSingle(
                        contentFrom(clients.getBy(serviceD), "http://localhost:8080/somethingElse", TEXT_HTML))
                ),
                fragment(E, withAll(
                        contentFrom(clients.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(clients.getBy(serviceC), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(clients.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(F, withSingle(
                        contentFrom(clients.getBy(serviceF), "http://localhost:8080/somethingElse", TEXT_HTML))
                )
        );
    }
}
