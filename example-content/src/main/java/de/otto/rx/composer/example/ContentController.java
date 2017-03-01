package de.otto.rx.composer.example;

import de.otto.rx.composer.client.Ref;
import de.otto.rx.composer.client.ServiceClients;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.page.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PreDestroy;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.client.ClientConfig.noRetries;
import static de.otto.rx.composer.client.ServiceClients.serviceClients;
import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.B;
import static de.otto.rx.composer.content.AbcPosition.C;
import static de.otto.rx.composer.content.AbcPosition.D;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
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
    private final ServiceClients client = serviceClients(
            noRetries(serviceA, 5000, 500),
            noRetries(serviceB, 5000, 400),
            noRetries(serviceC, 5000, 1000),
            noRetries(serviceD, 5000, 480),
            noRetries(serviceE, 5000, 550),
            noRetries(serviceF, 5000, 2000)
    );

    @PreDestroy
    public void shutdown() throws Exception {
        client.close();
    }

    @RequestMapping(path = "/", produces = "text/html")
    @ResponseBody
    public String getContent(final @RequestParam(required = false) String name) {
        final Page page = somePage();
        final Parameters parameters = name != null
                ? parameters(of("name", name))
                : emptyParameters();
        final Contents contents = page.fetchWith(parameters);
        return "<html>"
                + "<body>"
                + "<div id=A>" + contents.getBody(A) + "</div>"
                + "<div id=B>" + contents.getBody(B) + "</div>"
                + "</body></html>";
    }

    private Page somePage() {
        return consistsOf(
                fragment(A, withAll(
                        contentFrom(client.getBy(serviceA), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceA), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceA), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceA), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(B, withAll(
                        contentFrom(client.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceB), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(C, withAll(
                        contentFrom(client.getBy(serviceC), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceD), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceE), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML),
                        contentFrom(client.getBy(serviceF), fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML))
                ),
                fragment(D, withSingle(
                        contentFrom(client.getBy(serviceF), "http://localhost:8080/somethingElse", TEXT_HTML)))
        );
    }
}
