package liang.lollipop.llauncher.constant

import liang.lollipop.llauncher.BuildConfig

/**
 * @date: 2019-06-11 23:59
 * @author: lollipop
 *
 */
object ProviderConfig {

    var AUTHORITY = BuildConfig.APPLICATION_ID + ".settings"

    fun init(packageName: String) {
        AUTHORITY = "$packageName.settings"
    }
}
