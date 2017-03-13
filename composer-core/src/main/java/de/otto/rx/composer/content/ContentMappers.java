package de.otto.rx.composer.content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing mappers for {@link Content}.
 */
public class ContentMappers {

    /** Pattern used to extract the content. */
    private static final Pattern BODY_PATTERN = Pattern.compile("\\<body[^>]*?>([\\w|\\W]*?)<\\/body>(.*)");

    /**
     * Returns a {@link Content} instance that is only replacing {@link Content#getBody()} with the &lt;body&gt; of the
     * embedded {@code content} parameter.
     * <p>
     *     If Content is a {@link Content#isComposite() composite}, the concatenated bodies of all embedded contents
     *     is returned.
     * </p>
     * @param content the content delegate
     * @return content that is extracting the html body.
     */
    public static Content htmlBody(final Content content) {
        return new DelegatingContent(content) {
            @Override
            public String getBody() {
                return extractHtmlBody(content.getBody(), false);
            }
        };
    }

    private static String extractHtmlBody(final String body,
                                          final boolean onlyWithBodyElement) {
        final Matcher matcher = BODY_PATTERN.matcher(body);

        if (matcher.find()) {
            final String firstBody = matcher.group(1);
            if (matcher.groupCount() == 1) {
                return firstBody;
            } else {
                final String remainingBodies = matcher.group(2);
                return firstBody + extractHtmlBody(remainingBodies, true);
            }
        } else {
            return onlyWithBodyElement ? "" : body;
        }
    }

}
