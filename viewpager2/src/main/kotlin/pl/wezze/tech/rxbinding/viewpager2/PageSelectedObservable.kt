package pl.wezze.tech.rxbinding.viewpager2

import androidx.annotation.CheckResult
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun ViewPager2.pageSelections(): InitialValueObservable<Int> =
    PageSelectedObservable(this)

private class PageSelectedObservable(private val viewPager2: ViewPager2) : InitialValueObservable<Int>() {

    override val initialValue: Int
        get() = viewPager2.currentItem

    override fun subscribeListener(observer: Observer<in Int>) {
        RxPageChangeCallback(
            viewPager2,
            observer
        ).run {
            observer.onSubscribe(disposable)
            viewPager2.registerOnPageChangeCallback(this)
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

        override fun onPageSelected(position: Int) {
            if (!disposable.isDisposed) {
                observer.onNext(position)
            }
        }
    }
}
