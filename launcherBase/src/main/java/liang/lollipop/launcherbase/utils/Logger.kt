package liang.lollipop.launcherbase.utils

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

/**
 * @date: 2018/12/01 20:33
 * @author: lollipop
 *
 */
object Logger{

    fun D(tag: String, value: String) {
        Log.d(tag, value)
    }

    fun E(tag: String, value: String) {
        Log.e(tag, value)
    }

    fun I(tag: String, value: String) {
        Log.i(tag, value)
    }

    fun W(tag: String, value: String) {
        Log.w(tag, value)
    }

}

fun Activity.loggerD(value: String) {
    Logger.D("$packageName-${javaClass.name}", value)
}

fun Activity.loggerE(value: String) {
    Logger.E("$packageName-${javaClass.name}", value)
}

fun Activity.loggerI(value: String) {
    Logger.I("$packageName-${javaClass.name}", value)
}

fun Activity.loggerW(value: String) {
    Logger.W("$packageName-${javaClass.name}", value)
}

fun Fragment.loggerD(value: String) {
    Logger.D("${context?.packageName?:"fragment"}-${javaClass.name}", value)
}

fun Fragment.loggerE(value: String) {
    Logger.E("${context?.packageName?:"fragment"}-${javaClass.name}", value)
}

fun Fragment.loggerI(value: String) {
    Logger.I("${context?.packageName?:"fragment"}-${javaClass.name}", value)
}

fun Fragment.loggerW(value: String) {
    Logger.W("${context?.packageName?:"fragment"}-${javaClass.name}", value)
}

fun View.loggerD(value: String) {
    Logger.D("${context?.packageName?:"View"}-${javaClass.name}", value)
}

fun View.loggerE(value: String) {
    Logger.E("${context?.packageName?:"View"}-${javaClass.name}", value)
}

fun View.loggerI(value: String) {
    Logger.I("${context?.packageName?:"View"}-${javaClass.name}", value)
}

fun View.loggerW(value: String) {
    Logger.W("${context?.packageName?:"View"}-${javaClass.name}", value)
}
