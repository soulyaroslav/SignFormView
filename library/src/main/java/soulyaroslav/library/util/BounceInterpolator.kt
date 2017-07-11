package soulyaroslav.library.util

import android.view.animation.Interpolator

/**
 * Created by yaroslav on 7/5/17.
 */
class BounceInterpolator(val amplitude: Double, val frequency: Int) : Interpolator {

    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.pow(Math.E, -time / amplitude) * Math.cos(frequency.toDouble() * time) + 1).toFloat()
    }
}