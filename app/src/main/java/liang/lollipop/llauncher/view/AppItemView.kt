package liang.lollipop.llauncher.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup

/**
 * @date: 2019-06-16 02:41
 * @author: lollipop
 * 应用图标的按钮单元
 * 等价于 其他 Launcher 的 BubbleTextView
 * 拆分后的 View，将支持更多的样式，以及更多定制选项
 */
class AppItemView(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    companion object {
        private const val DEF_ICON_SIZE = 48F
        private const val DEF_FONT_SIZE = 13F
        private const val DEF_INTERVAL = 8F
    }

    /**
     * APP 的图标
     */
    private val iconView = AppIconView(context, attr, defStyleAttr, defStyleRes)

    /**
     * APP 的文本
     */
    private val textView = AppTextView(context, attr, defStyleAttr, defStyleRes)

    /**
     * 图标的尺寸
     * 默认情况下是 48dp
     * 但是为了适应不同的设备可能会对不同的设备做出调整，
     * 因此此处是 var 的，允许修改
     */
    var iconSize = dp(DEF_ICON_SIZE)
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 文字的尺寸
     * 默认情况下是 13 sp
     * 但是为了适应不同的设备可能会对不同的设备做出调整，
     * 因此此处是 var 的，允许修改
     */
    var fontSize = sp(DEF_FONT_SIZE)
        set(value) {
            field = value
            textView.textSize = value
            requestLayout()
        }

    /**
     * 文字与图标的间距
     * 初始化时提供默认的间距，但是针对不同的场景，可能会发生变化
     */
    var interval = dp(DEF_INTERVAL)
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 是否使用填充式的图标
     * 如果使用的话，那么 icon 的尺寸将会填充整个 view
     * 显示内容交由 icon 来确定
     */
    var isFullIcon = false
        set(value) {
            field = value
            iconView.isFullIcon = value
            textView.isFullIcon = value
            requestLayout()
        }

    init {
        addView(iconView)
        addView(textView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 直接设置传入尺寸作为自身的尺寸
        // 表示完全依赖父容器限定尺寸，或者填充的形式设置尺寸
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSpecSize, heightSpecSize)
        val widthSize = widthSpecSize - paddingLeft - paddingRight
        val heightSize = heightSpecSize - paddingTop - paddingBottom
        if (isFullIcon) {
            iconView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY))
        } else {
            iconView.measure(MeasureSpec.makeMeasureSpec(iconSize.toInt(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(iconSize.toInt(), MeasureSpec.EXACTLY))
        }
        textView.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = paddingLeft
        val top = paddingTop
        val widthSize = r - l - left - paddingRight
        val heightSize = b - t - top - paddingBottom

        val textLeft = (widthSize - textView.measuredWidth) / 2 + left
        val iconLeft = (widthSize - iconView.measuredWidth) / 2 + left

        val iconTop: Int
        val textTop: Int
        val iconHeight = iconView.measuredHeight
        val textHeight = textView.measuredHeight
        if (isFullIcon) {
            iconTop = (heightSize - iconHeight) / 2 + top
            textTop = heightSize - textHeight + top
        } else {
            val intervalSize = if (widthSize > heightSize) { 0F } else { interval }
            iconTop = ((heightSize - intervalSize - iconHeight - textHeight) / 2 + top).toInt()
            textTop = (iconTop + intervalSize + iconHeight).toInt()
        }
        iconView.layout(iconLeft, iconTop,
            iconLeft + iconView.measuredWidth, iconTop + iconView.measuredHeight)
        textView.layout(textLeft, textTop,
            textLeft + textView.measuredWidth, textTop + textView.measuredHeight)
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics)
    }

    private fun sp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            resources.displayMetrics)
    }
}