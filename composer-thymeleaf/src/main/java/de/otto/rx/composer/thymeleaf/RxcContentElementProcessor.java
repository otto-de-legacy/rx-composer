package de.otto.rx.composer.thymeleaf;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;

import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.thymeleaf.standard.expression.StandardExpressions.getExpressionParser;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * Thymeleaf 3 processor that replaces an element by the {@link de.otto.rx.composer.content.Content} at a specified
 * {@link de.otto.rx.composer.content.Position}:
 * <pre><code>
 *     &lt;rxc:content position="A" /&gt;
 * </code></pre>
 */
public class RxcContentElementProcessor extends AbstractElementTagProcessor {

    private static final Logger LOG = getLogger(RxcContentElementProcessor.class);

    public RxcContentElementProcessor() {
        super(HTML, "rxc", "fragment", true, "position", false, 1000);
    }

    @Override
    protected void doProcess(final ITemplateContext context,
                             final IProcessableElementTag tag,
                             final IElementTagStructureHandler structureHandler) {
        final Position pos = () -> tag.getAttributeValue("position");

        /*
         * In order to evaluate the attribute value as a Thymeleaf Standard Expression,
         * we first obtain the parser, then use it for parsing the attribute value into
         * an expression object, and finally execute this expression object.
         */
        final Contents contents;
        final String from = tag.getAttributeValue("from");
        if (from != null) {
            final IEngineConfiguration configuration = context.getConfiguration();
            final IStandardExpressionParser parser = getExpressionParser(configuration);
            final IStandardExpression expression = parser.parseExpression(context, from);
            contents = (Contents) expression.execute(context);
        } else {
            Object contentsVar = context.getVariable("contents");
            if (contentsVar != null) {
                contents = (Contents) contentsVar;
            } else {
                contents = null;
            }
        }

        if (contents != null) {
            final Content content = contents.get(pos);

            final Object debugMode = context.getVariable("debugMode");
            if (TRUE.equals(debugMode)) {
                // hier könnte man noch weitere Debug-Informationen ausgeben, einen Rahmen zeichnen, etc.
                structureHandler.replaceWith("<div style=\"border-style: solid; border-color: red\">\n" +
                        content.getBody() +
                        "<p style='border-top-style: solid; color: red'>" + content.getSource() + "</p>" +
                        "</div>", false);
            } else {
                // hier könnte man noch weitere Debug-Informationen ausgeben, einen Rahmen zeichnen, etc.
                structureHandler.replaceWith(content.getBody(), false);
            }
        } else {
            throw new IllegalStateException(
                    "Unable to get RxComposer Contents. " +
                    "You should either provide a template variable named 'contents' or " +
                    "provide it using attribute 'from': '<rxc:fragment from='${myContents}' position='A' />");
        }

    }

}
