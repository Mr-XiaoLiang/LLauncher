package liang.lollipop.llauncher.utils

import android.content.res.Resources
import android.util.TypedValue
import android.view.View

/**
 * 一些常用的工具方法
 * @author Lollipop
 */
object Utils {
}

val Resources.isRtl: Boolean
    get() {
        return this.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

fun Resources.dp(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, this.displayMetrics)
}

fun Float.boundToRange(min: Float, max: Float): Float {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

fun Int.boundToRange(min: Int, max: Int): Int {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}