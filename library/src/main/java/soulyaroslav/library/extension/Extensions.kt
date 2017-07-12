package soulyaroslav.library.extension

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.graphics.Rect
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.EditText
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by yaroslav on 7/4/17.
 */

fun ViewGroup.inflate(layoutId: Int, attach: Boolean = false) : View {
    return LayoutInflater.from(context).inflate(layoutId, this, attach)
}

fun EditText.onTextChange(onTextChange: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // no need
        }

        override fun onTextChanged(text: CharSequence?, start: Int, end: Int, count: Int) {
            onTextChange.invoke(text.toString())
        }

    })
}

inline fun ObjectAnimator.onEnd(crossinline func: () -> Unit) {
    addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) { func() }
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
    })
}

inline fun View.onClickEvent(crossinline func: () -> Unit) {
    setOnClickListener {
        func()
    }
}

fun View.rotate(start: Float, end: Float, delay: Long, polator: Interpolator?) : ObjectAnimator {
    val animator = ObjectAnimator.ofFloat(this, "rotation", start, end)
    animator.apply {
        duration = delay
        interpolator = polator
        start()
    }
    return animator
}

fun View.alpha(alpha: Float, delay: Long, polator: Interpolator?) : ObjectAnimator {
    val animator = ObjectAnimator.ofFloat(this, "alpha", alpha)
    animator.apply {
        duration = delay
        interpolator = polator
        start()
    }
    return animator
}

fun View.scaleX(scaleX: Float, delay: Long, polator: Interpolator?) : ObjectAnimator {
    val animator = ObjectAnimator.ofFloat(this, "scaleX", scaleX)
    animator.apply {
        duration = delay
        interpolator = polator
        start()
    }
    return animator
}

fun View.scaleY(scaleY: Float, delay: Long, polator: Interpolator?) : ObjectAnimator {
    val animator = ObjectAnimator.ofFloat(this, "scaleY", scaleY)
    animator.apply {
        duration = delay
        interpolator = polator
        start()
    }
    return animator
}

fun View.translateX(x: Float, delay: Long, sDelay: Long, polator: Interpolator?) : ObjectAnimator {
    val animator = ObjectAnimator.ofFloat(this, "translationX", x)
    animator.apply {
        duration = delay
        startDelay = sDelay
        interpolator = polator
        start()
    }
    return animator
}

fun View.shake(x1: Float, x2: Float, x3: Float, x4: Float, x5: Float, repeat: Int = 0, delay: Long, polator: Interpolator?) {
    val animator = ObjectAnimator.ofFloat(this, "translationX", x1, x2, x3, x4, x5)
    animator.apply {
        repeatCount = repeat
        duration = delay
        interpolator = polator
        start()
    }
}

fun TextPaint.textWidth(text: String) : Int {
    val bounds = Rect()
    getTextBounds(text, 0, text.length, bounds)
    return bounds.width()
}

//infix fun Int.isLess(x: Int) : Boolean {
//    return this < x
//}
//
//inline fun f(crossinline body: () -> Unit) {
//    val f = Runnable { body() }
//    // ...
//}

private inline fun <T> SharedPreferences.delegate(defaultValue: T, key: String?,
                                                  crossinline getter: SharedPreferences.(String, T) -> T,
                                                  crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>) =
                getter(key ?: property.name, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
                edit().setter(key ?: property.name, value).apply()
    }
}

fun SharedPreferences.int(def: Int = 0, key: String? = null) =
        delegate(def, key, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

fun SharedPreferences.string(def: String = "", key: String? = null) =
        delegate(def, key, SharedPreferences::getString, SharedPreferences.Editor::putString)