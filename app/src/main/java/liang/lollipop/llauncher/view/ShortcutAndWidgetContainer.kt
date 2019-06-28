package liang.lollipop.llauncher.view

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import liang.lollipop.llauncher.utils.isRtl
import kotlin.reflect.KProperty

/**
 * @date: 2019-06-16 00:04
 * @author: lollipop
 * 桌面上组件和图标真正排列的 View
 * 用于包装排版的逻辑，简化 CellLayout 的代码逻辑
 * 本 View 的布局不允许 Margin，每个 Child 的边距将没有间距
 * 如果希望展示间距，那么将通过 padding 来实现。以此来简化排版的过程
 * 并且使间距的显示变得可控，样式的管理变得可控
 */
class ShortcutAndWidgetContainer(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attr, defStyleAttr, defStyleRes) {
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    /**
     * 行数
     */
    var yCount = 5
        set(value) {
            field = value
            requestLayout()
        }
    /**
     * 列数
     */
    var xCount = 4
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 用于临时计算的坐标数组，
     * 用于减少因为临时计算造成的频繁数组创建
     */
    private val tmpCellXY = IntArray(2)

    /**
     * 单元格的宽度
     */
    private var cellWidth = 0

    /**
     * 单元格的高度
     */
    private var cellHeight = 0

    /**
     * 是否允许横向翻转（从右到左的排列
     */
    var invertHorizontally = true

    /**
     * 是否需要横向翻转
     * 这里将依赖 invertHorizontally 属性，
     * 如果不允许，那么将强制锁定从左到右
     */
    private val needInvertX: Boolean
        get() { return invertHorizontally && resources.isRtl }

    /**
     * 壁纸管理器
     */
    private val wallpaperManager = WallpaperManager.getInstance(context)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 直接设置传入尺寸作为自身的尺寸
        // 表示完全依赖父容器限定尺寸，或者填充的形式设置尺寸
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthSpecSize, heightSpecSize)
        val layoutHeight = heightSpecSize - paddingTop - paddingBottom
        val layoutWidth = widthSpecSize - paddingLeft - paddingRight
        cellWidth = (layoutWidth * 1F / xCount).toInt()
        cellHeight = (layoutHeight * 1F / yCount).toInt()
        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)
            if (child.visibility != View.GONE) {
                measureChild(child)
            }
        }
    }

    /**
     * 只根据 LayoutParams 来计算宽度和高度，
     * 然后将一切交给 child
     * 自身根据填充属性，来修改 span 的数值
     * 尽可能少的参与子 view 的测量过程
     * 保证更多的自由度和扩展性
     */
    private fun measureChild(child: View) {
        val childLp = child.layoutParams as CellLayout.LayoutParams
        if (childLp.isFullHorizontal) {
            childLp.cellHSpan = xCount
            childLp.x = 0
        }
        if (childLp.isFullVertical) {
            childLp.cellVSpan = yCount
            childLp.y = 0
        }
        childLp.setup(cellWidth, cellHeight, xCount, yCount, needInvertX)
        child.measure(MeasureSpec.makeMeasureSpec(childLp.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(childLp.height, MeasureSpec.EXACTLY))
    }

    fun getChildAt(x: Int, y: Int): View? {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val lp = child.layoutParams as CellLayout.LayoutParams

            if (lp.cellX <= x && x < lp.cellX + lp.cellHSpan &&
                lp.cellY <= y && y < lp.cellY + lp.cellVSpan
            ) {
                return child
            }
        }
        return null
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val offsetX = paddingLeft
        val offsetY = paddingTop
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            val lp = child.layoutParams as CellLayout.LayoutParams
            val childLeft = lp.x + offsetX
            val childTop = lp.y + offsetY
            child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height)

            if (lp.dropped) {
                lp.dropped = false

                val cellXY = tmpCellXY
                getLocationOnScreen(cellXY)
                wallpaperManager.sendWallpaperCommand(windowToken,
                    WallpaperManager.COMMAND_DROP,
                    cellXY[0] + childLeft + lp.width / 2,
                    cellXY[1] + childTop + lp.height / 2,
                    0, null)
            }
        }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun requestChildFocus(child: View?, focused: View) {
        super.requestChildFocus(child, focused)
        if (child != null) {
            val r = Rect()
            child.getDrawingRect(r)
            requestRectangleOnScreen(r)
        }
    }

    override fun cancelLongPress() {
        super.cancelLongPress()

        // 取消所有子 view 的长按状态
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            child.cancelLongPress()
        }
    }

    override fun setLayerType(layerType: Int, paint: Paint?) {
        // 如果不对子 view 进行剪裁，那么禁用硬件加速，因为硬件加速将强制剪裁子 view
        super.setLayerType(if (clipChildren) { layerType } else { View.LAYER_TYPE_NONE }, paint)
    }

}