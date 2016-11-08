package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableSet;
import jersey.repackaged.com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.Sets.newConcurrentHashSet;

public final class Contents {

    private final ConcurrentMap<ContentPosition, Content> results = new ConcurrentHashMap<>();
    private final Set<ContentPosition> empty = newConcurrentHashSet();
    private final Set<ContentPosition> errors = newConcurrentHashSet();

    public void add(final Content content) {
        switch (content.status()) {
            case OK:
                results.put(content.getContentPosition(), content);
                break;
            case EMPTY:
                empty.add(content.getContentPosition());
                break;
            case ERROR:
                errors.add(content.getContentPosition());
        }
    }

    public ImmutableList<Content> getContents() {
        return ImmutableList.copyOf(results.values());
    }

    public Optional<Content> getContent(final ContentPosition contentPosition) {
        return Optional.ofNullable(results.get(contentPosition));
    }

    public ImmutableSet<ContentPosition> getEmpty() {
        return ImmutableSet.copyOf(empty);
    }

    public ImmutableSet<ContentPosition> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
