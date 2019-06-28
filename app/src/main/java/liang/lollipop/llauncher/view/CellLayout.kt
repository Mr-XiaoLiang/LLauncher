package liang.lollipop.llauncher.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import liang.lollipop.llauncher.data.CellAndSpan
import kotlin.reflect.KProperty

/**
 * @date: 2019-06-10 22:57
 * @author: lollipop
 * 桌面每个分页的 layout
 */
class CellLayout(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    /**
     * View 组件排版的真正的容器
     */
    private val shortcutAndWidgetContainer = ShortcutAndWidgetContainer(context, attr, defStyleAttr, defStyleRes)

    /**
     * 行数
     */
    var yCount: Int
        set(value) { shortcutAndWidgetContainer.yCount = value }
        get() { return shortcutAndWidgetContainer.yCount }

    /**
     * 列数
     */
    var xCount: Int
        set(value) { shortcutAndWidgetContainer.xCount = value }
        get() { return shortcutAndWidgetContainer.xCount }

    init {
        addView(shortcutAndWidgetContainer)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = paddingLeft
        val top = paddingTop
        val right = r - l - paddingRight
        val bottom = b - t - paddingBottom
        shortcutAndWidgetContainer.layout(left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw RuntimeException("CellLayout cannot have UNSPECIFIED dimensions")
        }
        val layoutWidth = widthSize - paddingLeft - paddingRight
        val layoutHeight = heightSize - paddingTop - paddingBottom

        shortcutAndWidgetContainer.measure(MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.EXACTLY))
        setMeasuredDimension(widthSize, heightSize)
    }

    class LayoutParams: ViewGroup.LayoutParams {

        /**
         * 左上角起点坐标 X
         * 单位：格
         */
        var cellX = 0
        /**
         * 左上角起点坐标 Y
         * 单位：格
         */
        var cellY = 0

        /**
         * 临时坐标的
         * 左上角起点坐标 X
         * 单位：格
         */
        var tmpCellX = 0
        /**
         * 临时坐标的
         * 左上角起点坐标 Y
         * 单位：格
         */
        var tmpCellY = 0

        /**
         * 使用临时坐标
         */
        var useTmpCoords = false

        /**
         * 横向宽度占用
         * 单位：格
         */
        var cellHSpan = 1

        /**
         * 纵向高度占用
         * 单位：格
         */
        var cellVSpan = 1

        /**
         * view 在布局中的 X 坐标
         */
        var x = 0

        /**
         * view 在布局中的 Y 坐标
         */
        var y = 0

        /**
         * 是否按照格子排列
         * 默认为 true
         */
        var isLockedToGrid = true

        /**
         * 是否可以移动，默认为 true
         * 部分固定组件为 false，不允许移动
         */
        var canReorder = true

        /**
         * 是否填充满横向
         * 优先级最高，当位 true 时，将忽略 cellHSpan
         */
        var isFullHorizontal = false

        /**
         * 是否填充满纵向
         * 优先级最高，当位 true 时，将忽略 cellVSpan
         */
        var isFullVertical = false

        /**
         * 是否正在被拖拽
         */
        var dropped = false

        constructor(c: Context, attr: AttributeSet?): super(c, attr)

        constructor(source: ViewGroup.LayoutParams): super(source)

        constructor(source: LayoutParams): super(source) {
            this.cellX = source.cellX
            this.cellY = source.cellY
            this.cellHSpan = source.cellHSpan
            this.cellVSpan = source.cellVSpan
            this.isLockedToGrid = source.isLockedToGrid
            this.canReorder = source.canReorder
            this.isFullHorizontal = source.isFullHorizontal
            this.isFullVertical = source.isFullVertical
        }

        /**
         * 触发获取组件的尺寸
         * 本类不考虑 margin 以及间隔
         * 即，CellLayout 中，不允许子 View 设置 Margin。
         * 单元格也不会存在间隔。
         * 所有组件之间的留白都是通过自身的 Padding 完成。
         */
        public fun setup(cellWidth: Int, cellHeight: Int,
                         xCount: Int, yCount: Int,
                         invertHorizontally: Boolean) {
            if (isLockedToGrid) {
                var myCellX = if (useTmpCoords) { tmpCellX } else { cellX }
                val myCellY = if (useTmpCoords) { tmpCellY } else { cellY }

                if (invertHorizontally) {
                    myCellX = xCount - myCellX - cellHSpan
                }

                width = cellHSpan * cellWidth
                height = cellVSpan * cellHeight

                x = myCellX * cellWidth
                y = myCellY * cellHeight
            }
        }
    }

}
