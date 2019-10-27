package com.github.ddmytrenko.rxbinding.viewpager2

import androidx.annotation.CheckResult
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun ViewPager2.pageScrollStateChanges(): Observable<Int> =
    PageScrollStateChangedObservable(this)

private class PageScrollStateChangedObservable(private val viewPager2: ViewPager2) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (checkMainThread(observer)) {
            RxPageChangeCallback(viewPager2, observer).run {
                observer.onSubscribe(disposable)
                viewPager2.registerOnPageChangeCallback(this)
            }
        }
    }

    private class RxPageChangeCallback(
        private val viewPager2: ViewPager2,
        private val observer: Observer<in Int>
    ) : OnPageChangeCallback() {

        val disposable = object : MainThreadDisposable() {
            override fun onDispose() {
                viewPager2.unregisterOnPageChangeCallback(this@RxPageChangeCallback)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (!disposable.isDisposed) {
                observer.onNext(state)
            }
        }
    }
}
