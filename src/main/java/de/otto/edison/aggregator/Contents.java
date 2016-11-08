package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableSet;
import jersey.repackaged.com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.collect.Sets.newConcurrentHashSet;

public final class Contents {

    private final ConcurrentMap<Position, Content> results = new ConcurrentHashMap<>();
    private final Set<Position> empty = newConcurrentHashSet();
    private final Set<Position> errors = newConcurrentHashSet();

    public void add(final Content content) {
        switch (content.status()) {
            case OK:
                results.put(content.getPosition(), content);
                break;
            case EMPTY:
                empty.add(content.getPosition());
                break;
            case ERROR:
                errors.add(content.getPosition());
        }
    }

    public ImmutableList<Content> getContents() {
        return ImmutableList.copyOf(results.values());
    }

    public Optional<Content> getContent(final Position position) {
        return Optional.ofNullable(results.get(position));
    }

    public ImmutableSet<Position> getEmpty() {
        return ImmutableSet.copyOf(empty);
    }

    public ImmutableSet<Position> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
