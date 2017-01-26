package de.otto.rx.composer.providers;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import de.otto.rx.composer.client.Ref;
import de.otto.rx.composer.content.Content;
import rx.Observable;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static com.netflix.hystrix.HystrixCommandProperties.Setter;

class HystrixObservableContent extends HystrixObservableCommand<Content> {

    private Observable<Content> observable;
    private Observable<Content> fallback;

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
                        //.withExecutionIsolationStrategy(SEMAPHORE)
                        //.withExecutionIsolationSemaphoreMaxConcurrentRequests(100)
                        .withExecutionTimeoutInMilliseconds(timeoutMillis)
                )
        );
        this.observable = observable;
        this.fallback = fallback;
    }

    @Override
    protected Observable<Content> construct() {
        return observable;
    }

    @Override
    protected Observable<Content> resumeWithFallback() {
        return fallback != null ? fallback : super.resumeWithFallback();
    }
}