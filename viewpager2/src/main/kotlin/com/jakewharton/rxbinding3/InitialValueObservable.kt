package com.jakewharton.rxbinding3

import io.reactivex.Observable
import io.reactivex.Observer

/**
 * InitialValueObservable from Jake Wharton's RxBinding
 */
abstract class InitialValueObservable<T> : Observable<T>() {
    protected abstract val initialValue: T

    override fun subscribeActual(observer: Observer<in T>) {
        subscribeListener(observer)
        observer.onNext(initialValue)
    }

    protected abstract fun subscribeListener(observer: Observer<in T>)

    fun skipInitialValue(): Observable<T> = Skipped()

    private inner class Skipped : Observable<T>() {
        override fun subscribeActual(observer: Observer<in T>) {
            subscribeListener(observer)
        }
    }
}
