package de.otto.rx.composer.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 */
public class ContentMatcher implements Predicate<Content> {

    private static final Logger LOG = LoggerFactory.getLogger(ContentMatcher.class);

    private final Predicate<Content> predicate;
    private final String mismatchDescription;

    private ContentMatcher(final Predicate<Content> predicate,
                           final String mismatchDescription) {
        this.predicate = predicate;
        this.mismatchDescription = mismatchDescription != null ? mismatchDescription : "(no description)";
    }

    public static ContentMatcher contentMatcher(final Predicate<Content> predicate,
                                                final String mismatchDescription) {
        return new ContentMatcher(predicate, mismatchDescription);
    }

    public String getMismatchDescription() {
        return mismatchDescription;
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param content the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(final Content content) {
        if (predicate.test(content)) {
            LOG.trace("Content from {}", content.getSource() + " is matching");
            return true;
        } else {
            LOG.trace("Ignoring content {}: {}", content.getSource(), mismatchDescription);
            return false;
        }
    }

}
