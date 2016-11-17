package de.otto.aggregator.example;

import de.otto.edison.aggregator.Plan;
import de.otto.edison.aggregator.content.Contents;
import de.otto.edison.aggregator.http.HttpClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static de.otto.edison.aggregator.Plan.planIsTo;
import static de.otto.edison.aggregator.content.AbcPosition.A;
import static de.otto.edison.aggregator.content.AbcPosition.B;
import static de.otto.edison.aggregator.content.Parameters.emptyParameters;
import static de.otto.edison.aggregator.providers.ContentProviders.fetchViaHttpGet;
import static de.otto.edison.aggregator.steps.Steps.forPos;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;

@Controller
public class ContentController {

    private final HttpClient client = new HttpClient(1000, 1000);

    @RequestMapping(path = "/", produces = "text/html")
    @ResponseBody
    public String getContent() {
        final Plan plan = defaultPlan();
        final Contents contents = plan.execute(emptyParameters());
        return "<html>"
                + "<body>"
                + "<div id=A>" + contents.getContent(A).get().getBody() + "</div>"
                + "<div id=B>" + contents.getContent(B).get().getBody() + "</div>"
                + "</body></html>";
    }

    private Plan defaultPlan() {
        return planIsTo(
                forPos(A, fetchViaHttpGet(client, "http://localhost:8080/hello", TEXT_HTML_TYPE)),
                forPos(B, fetchViaHttpGet(client, "http://localhost:8080/somethingElse", TEXT_HTML_TYPE))
        );
    }
}
