package de.otto.rx.composer.example;

import de.otto.rx.composer.Plan;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.http.HttpClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.Plan.planIsTo;
import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.AbcPosition.B;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static de.otto.rx.composer.providers.ContentProviders.fetchViaHttpGet;
import static de.otto.rx.composer.steps.Steps.forPos;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;

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
        final Plan plan = defaultPlan();
        final Parameters parameters = name != null
                ? parameters(of("name", name))
                : emptyParameters();
        final Contents contents = plan.execute(parameters);
        return "<html>"
                + "<body>"
                + "<div id=A>" + contents.getBody(A) + "</div>"
                + "<div id=B>" + contents.getBody(B) + "</div>"
                + "</body></html>";
    }

    private Plan defaultPlan() {
        return planIsTo(
                forPos(A, fetchViaHttpGet(client, fromTemplate("http://localhost:8080/hello{?name}"), TEXT_HTML_TYPE)),
                forPos(B, fetchViaHttpGet(client, "http://localhost:8080/somethingElse", TEXT_HTML_TYPE))
        );
    }
}
