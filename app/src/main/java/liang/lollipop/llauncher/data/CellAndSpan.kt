package liang.lollipop.llauncher.data

/**
 * @date: 2019-06-11 22:20
 * @author: lollipop
 * 基类，用于宫格布局，包含位置以及尺寸占用
 */
open class CellAndSpan(
    /** X 轴的位置，单位为格 **/
    var cellX: Int = -1,
    /** Y 轴的位置，单位为格 **/
    var cellY: Int = -1,
    /** 占用宽度，单位为格 **/
    var spanX: Int = 1,
    /** 占用高度，单位为格 **/
    var spanY: Int = 1) {

    fun copyFrom(copy: CellAndSpan) {
        cellX = copy.cellX
        cellY = copy.cellY
        spanX = copy.spanX
        spanY = copy.spanY
    }

    override fun toString(): String {
        return "($cellX, $cellY: $spanX, $spanY)"
    }

}