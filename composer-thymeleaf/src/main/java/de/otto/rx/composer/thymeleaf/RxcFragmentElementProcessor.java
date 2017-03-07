package de.otto.rx.composer.thymeleaf;

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

import static org.slf4j.LoggerFactory.getLogger;
import static org.thymeleaf.standard.expression.StandardExpressions.getExpressionParser;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * Thymeleaf 3 element processor that inserts the rx-composer {@link de.otto.rx.composer.content.Content} at a specified
 * {@link de.otto.rx.composer.content.Position}:
 * <pre><code>
 *     &lt;rxc:content position="A" /&gt;
 * </code></pre>
 */
public class RxcFragmentElementProcessor extends AbstractElementTagProcessor {

    private static final Logger LOG = getLogger(RxcFragmentElementProcessor.class);

    public static final String ATTR_POSITION = "position";
    public static final String ATTR_FROM = "from";
    public static final String DEFAULT_VAR_CONTENTS = "contents";
    public static final String RXC_DIALECT = "rxc";
    public static final String ELEMENT_NAME = "fragment";


    public RxcFragmentElementProcessor() {
        super(HTML, RXC_DIALECT, ELEMENT_NAME, true, ATTR_POSITION, false, 1000);
    }

    @Override
    protected void doProcess(final ITemplateContext context,
                             final IProcessableElementTag tag,
                             final IElementTagStructureHandler structureHandler) {
        final Contents contents = getContents(context, tag);
        final Position pos = getPosition(context, tag);
        structureHandler.replaceWith(contents.get(pos).getBody(), false);
    }

    static Position getPosition(final ITemplateContext context,
                                final IProcessableElementTag tag) {
        String position = tag.getAttributeValue("position");
        try {
            final IEngineConfiguration configuration = context.getConfiguration();
            final IStandardExpressionParser parser = getExpressionParser(configuration);
            final IStandardExpression expression = parser.parseExpression(context, position);
            return () -> expression.execute(context).toString();
        } catch (final Exception e) {
            return () -> position;
        }
    }

    static Contents getContents(final ITemplateContext context,
                                final IProcessableElementTag tag) {
        final Contents contents;
        final String from = tag.getAttributeValue(ATTR_FROM);
        if (from != null) {
            final IEngineConfiguration configuration = context.getConfiguration();
            final IStandardExpressionParser parser = getExpressionParser(configuration);
            final IStandardExpression expression = parser.parseExpression(context, from);
            contents = (Contents) expression.execute(context);
        } else {
            Object contentsVar = context.getVariable(DEFAULT_VAR_CONTENTS);
            if (contentsVar != null) {
                contents = (Contents) contentsVar;
            } else {
                contents = null;
            }
        }
        if (contents != null) {
            return contents;
        } else {
            throw new IllegalStateException(
                    "Unable to get RxComposer Contents. " +
                            "You should either provide a template variable named 'contents' or " +
                            "provide it using attribute 'from': '<rxc:fragment from='${myContents}' position='A' />");
        }
    }

}
