package de.otto.edison.aggregator;

import rx.Subscriber;

import java.util.function.Consumer;

public class ContentSubscriber extends Subscriber<Content> {

    private final Consumer<Contents> consumer;
    private final Contents contents;

    private ContentSubscriber(final Consumer<Contents> consumer) {
        this.consumer = consumer;
        this.contents = new Contents();
    }

    public static ContentSubscriber contentSubscriber(final Consumer<Contents> consumer) {
        return new ContentSubscriber(consumer);
    }

    @Override
    public void onCompleted() {
        consumer.accept(contents);
    }

    @Override
    public void onError(final Throwable e) {
    }

    @Override
    public void onNext(final Content content) {
        contents.add(content);
    }
}
