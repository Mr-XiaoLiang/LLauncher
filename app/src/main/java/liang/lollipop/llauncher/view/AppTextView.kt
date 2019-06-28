package liang.lollipop.llauncher.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * @date: 2019-06-16 02:38
 * @author: lollipop
 * APP 按钮的文字展示的 View
 * 单独包装为 View 的原因在于，可以更加简单的定义文本的样式以及特性
 * 它将支持字体的自定义，样式的自定义，以及新应用提示功能
 */
class AppTextView(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    var textSize: Float = 0F

    var isFullIcon = false

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // TODO("not implemented") To change body of created functions use File | Settings | File Templates.
    }

}