package liang.lollipop.llauncher.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * @date: 2019-06-16 02:31
 * @author: lollipop
 * 应用的图标 View。用于展示应用图标，文件夹图标，或者其他按钮等
 */
class AppIconView(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    View(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    var isFullIcon = false
}