package de.otto.rx.composer.providers;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import de.otto.rx.composer.client.Ref;
import de.otto.rx.composer.content.Content;
import rx.Observable;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE;
import static com.netflix.hystrix.HystrixCommandProperties.Setter;

class HystrixObservableContent extends HystrixObservableCommand<Content> {

    public static final int MAX_CONCURRENT_REQUESTS_PER_COMMANDKEY = 100;

    private Observable<Content> observable;
    private Observable<Content> fallback;
    private AtomicBoolean firstExecution = new AtomicBoolean(true);

    public static Observable<Content> from(final Observable<Content> observable,
                                           final Ref commandKey,
                                           final int timeoutMillis) {
        return new HystrixObservableContent(observable, null, commandKey, timeoutMillis).toObservable();
    }

    public static Observable<Content> from(final Observable<Content> observable,
                                           final Observable<Content> fallback,
                                           final Ref commandKey,
                                           final int timeoutMillis) {
        return new HystrixObservableContent(observable, fallback, commandKey, timeoutMillis).toObservable();
    }

    private HystrixObservableContent(final Observable<Content> observable,
                                     final Observable<Content> fallback,
                                     final Ref commandKey,
                                     final int timeoutMillis) {
        super(Setter.withGroupKey(asKey(commandKey.name()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey.name()))
                .andCommandPropertiesDefaults(Setter()
                        .withExecutionIsolationStrategy(SEMAPHORE)
                        // this configures the max number of concurrent requests per command:
                        // TODO: make it easier to use different commands (one per service)
                        // TODO: make this value configurable
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(MAX_CONCURRENT_REQUESTS_PER_COMMANDKEY)
                        .withExecutionTimeoutInMilliseconds(timeoutMillis)
                )
        );
        this.observable = observable;
        this.fallback = fallback;
    }

    @Override
    protected Observable<Content> construct() {
        if (!firstExecution.get()) {
            throw new IllegalStateException("Must not execute twice");
        }
        firstExecution.set(false);
        return observable;
    }

    @Override
    protected Observable<Content> resumeWithFallback() {
        return fallback != null ? fallback : super.resumeWithFallback();
    }
}