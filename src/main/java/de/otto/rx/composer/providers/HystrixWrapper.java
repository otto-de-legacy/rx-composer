package de.otto.rx.composer.providers;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;
import static com.netflix.hystrix.HystrixCommandProperties.Setter;
import static rx.Observable.just;

class HystrixWrapper<T> extends HystrixObservableCommand<T> {

    private Observable<T> observable;
    private Observable<T> fallback;

    public static <T> Observable<T> from(final Observable<T> observable,
                                         final T fallbackValue,
                                         final String commandKey,
                                         final int timeoutMillis) {
        return new HystrixWrapper<>(observable, just(fallbackValue), commandKey, timeoutMillis).toObservable();
    }

    public static <T> Observable<T> from(final Observable<T> observable,
                                         final String commandKey,
                                         final int timeoutMillis) {
        return new HystrixWrapper<>(observable, null, commandKey, timeoutMillis).toObservable();
    }

    public static <T> Observable<T> from(final Observable<T> observable,
                                         final Observable<T> fallback,
                                         final String commandKey,
                                         final int timeoutMillis) {
        return new HystrixWrapper<>(observable, fallback, commandKey, timeoutMillis).toObservable();
    }

    private HystrixWrapper(final Observable<T> observable,
                           final Observable<T> fallback,
                           final String commandKey,
                           final int timeoutMillis) {
        super(Setter.withGroupKey(asKey(commandKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
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
    protected Observable<T> construct() {
        return observable;
    }

    @Override
    protected Observable<T> resumeWithFallback() {
        return fallback != null ? fallback : super.resumeWithFallback();
    }
}