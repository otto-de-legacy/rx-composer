package de.otto.rx.composer.content;


import org.junit.Test;

import java.time.LocalDateTime;

import static de.otto.rx.composer.content.AbcPosition.A;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SingleContentTest {

    @Test
    public void shouldConvertToSingleContent() {
        final Content content = someSingleContent();
        assertThat(content.isComposite(), is(false));
        assertThat(content.asSingle(), is(notNullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailToConvertToCompositeContent() {
        final Content content = someSingleContent();
        content.asComposite();
    }

    private Content someSingleContent() {
        return new SingleContent() {
            @Override
            public Position getPosition() {
                return A;
            }

            @Override
            public String getSource() {
                return "Foo";
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String getBody() {
                return "Bar";
            }

            @Override
            public Headers getHeaders() {
                return emptyHeaders();
            }

            @Override
            public LocalDateTime getCreated() {
                return now();
            }
        };
    }

}