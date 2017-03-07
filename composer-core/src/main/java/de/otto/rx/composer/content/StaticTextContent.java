package de.otto.rx.composer.content;

import static de.otto.rx.composer.content.Headers.emptyHeaders;

/**
 * Static Content that may be used as fallback content.
 */
public class StaticTextContent extends SingleContent {
    private final String source;
    private final String text;
    private final Position position;

    public StaticTextContent(final Position position,
                             final String text) {
        this.source = "Static Text";
        this.position = position;
        this.text = text;
    }

    private StaticTextContent(final String source,
                              final Position position,
                              final String text) {
        this.source = source;
        this.position = position;
        this.text = text;
    }

    public static StaticTextContent staticTextContent(final String source,
                                                      final Position position,
                                                      final String text) {
        return new StaticTextContent(source, position, text);
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isAvailable() {
        return !text.isEmpty();
    }

    @Override
    public String getBody() {
        return text;
    }

    @Override
    public Headers getHeaders() {
        return emptyHeaders();
    }

    @Override
    public long getStartedTs() {
        return 0L;
    }

    @Override
    public long getCompletedTs() {
        return 0L;
    }
}
