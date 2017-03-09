package de.otto.rx.composer.thymeleaf;

import de.otto.rx.composer.content.*;
import org.junit.Test;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import java.io.IOException;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Contents.contentsBuilder;
import static de.otto.rx.composer.content.StaticTextContent.staticTextContent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RxcFragmentElementProcessorTest {

    @Test
    public void shouldReturnContent() throws IOException {
        Contents contents = contentsBuilder()
                .add(someContent(A))
                .build();

        final ITemplateContext context = mock(ITemplateContext.class);
        //when(context.getVariable("debugMode")).thenReturn(FALSE);
        when(context.getVariable("contents")).thenReturn(contents);

        final IProcessableElementTag tag = mock(IProcessableElementTag.class);
        when(tag.getAttributeValue("position")).thenReturn("A");


        final IElementTagStructureHandler structureHandler = mock(IElementTagStructureHandler.class);

        //when
        new RxcFragmentElementProcessor().doProcess(context, tag, structureHandler);

        //then
        verify(structureHandler).replaceWith("Some Content", false);
    }

    private Content someContent(final Position position) {
        return staticTextContent(position.name(), position, "Some Content");
    }
}