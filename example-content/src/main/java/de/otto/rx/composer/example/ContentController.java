package de.otto.rx.composer.example;

import de.otto.rx.composer.page.Page;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.http.HttpClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.page.Page.consistsOf;
import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.B;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static de.otto.rx.composer.providers.ContentProviders.*;
import static de.otto.rx.composer.page.Fragments.*;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Fetches two contents from two different "microservices". An optional request-parameter 'name' is forwarded to the
 * 'hello' service, which is returning 'Hello &lt;name&gt;'.
 */
@Controller
public class ContentController {

    private final HttpClient client = new HttpClient(1000, 1000);

    @RequestMapping(path = "/", produces = "text/html")
    @ResponseBody
    public String getContent(final @RequestParam(required = false) String name) {
        final Page page = defaultPlan();
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

    private Page defaultPlan() {
        return consistsOf(
                fragment(A, withSingle(contentFrom(client, fromTemplate("http://localhost:8080/hello{?name}"), TEXT_PLAIN))),
                fragment(B, withSingle(contentFrom(client, "http://localhost:8080/somethingElse", TEXT_PLAIN)))
        );
    }
}
