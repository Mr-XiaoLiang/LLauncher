package liang.lollipop.llauncher.view

/**
 * @date: 2019-05-02 15:42
 * @author: lollipop
 * 分页的接口，用于表示桌面的某一页
 * 此接口申明对于分页身份的必要责任，但不保证身份的唯一性
 */
interface Page {

    /**
     * 是否允许删除
     * 如果允许删除，那么当前分页将会允许向上滑动来删除
     */
    fun canRemove(): Boolean

    /**
     * 是否允许移动
     * 如果允许移动，那么将会使当前 Page 被拖拽或者被交换位置
     */
    fun canMove(): Boolean

}