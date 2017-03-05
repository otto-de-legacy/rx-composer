package de.otto.rx.composer.thymeleaf;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Contents;
import de.otto.rx.composer.content.Position;
import org.slf4j.Logger;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;
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

        // wahrscheinlich sollte man das noch umbauen; die Variable muss ja nicht immer "contents" heißen...
        final Contents contents = (Contents) context.getVariable("contents");
        final Content content = contents.get(pos);

        final Object debugMode = context.getVariable("debugMode");
        if (TRUE.equals(debugMode)) {
            // hier könnte man noch weitere Debug-Informationen ausgeben, einen Rahmen zeichnen, etc.
            structureHandler.replaceWith("<b>DEBUG...</b>\n" + content.getBody(),false);
        } else {
            // hier könnte man noch weitere Debug-Informationen ausgeben, einen Rahmen zeichnen, etc.
            structureHandler.replaceWith(content.getBody(),false);
        }

    }

}
