package com.jakewharton.rxbinding3;

import android.util.Log;

import io.reactivex.observers.DisposableObserver;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * RecordingObserver from Jake Wharton's RxBinding test-utils
 */
public final class RecordingObserver<T> extends DisposableObserver<T> {
    private static final String TAG = "RecordingObserver";

    private final BlockingDeque<Object> events = new LinkedBlockingDeque<>();

    @Override
    public void onComplete() {
        Log.v(TAG, "onCompleted");
        events.addLast(new OnCompleted());
    }

    @Override
    public void onError(Throwable e) {
        Log.v(TAG, "onError", e);
        events.addLast(new OnError(e));
    }

    @Override
    public void onNext(T t) {
        Log.v(TAG, "onNext " + t);
        events.addLast(new OnNext(t));
    }

    private <E> E takeEvent(Class<E> wanted) {
        Object event;
        try {
            event = events.pollFirst(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (event == null) {
            throw new NoSuchElementException(
                    "No event found while waiting for " + wanted.getSimpleName());
        }
        assertTrue(wanted.isInstance(event));
        return wanted.cast(event);
    }

    public T takeNext() {
        OnNext event = takeEvent(OnNext.class);
        return event.value;
    }

    public Throwable takeError() {
        return takeEvent(OnError.class).throwable;
    }

    public void clearEvents() {
        while (events.peek() instanceof RecordingObserver.OnNext) {
            events.removeFirst();
        }
    }

    public void assertOnCompleted() {
        takeEvent(OnCompleted.class);
    }

    public void assertNoMoreEvents() {
        try {
            Object event = takeEvent(Object.class);
            throw new IllegalStateException("Expected no more events but got " + event);
        } catch (NoSuchElementException ignored) {
        }
    }

    private final class OnNext {
        final T value;

        private OnNext(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "OnNext[" + value + "]";
        }
    }

    private final class OnCompleted {
        @Override
        public String toString() {
            return "OnCompleted";
        }
    }

    private final class OnError {
        private final Throwable throwable;

        private OnError(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public String toString() {
            return "OnError[" + throwable + "]";
        }
    }
}
