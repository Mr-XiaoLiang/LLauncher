package liang.lollipop.llauncher.constant

/**
 * @date: 2019-06-11 23:33
 * @author: lollipop
 * 桌面元素的类型
 */
enum class ItemType(val value: Int) {
    APPLICATION(0),
    SHORTCUT(1),
    FOLDER(3),
    APPWIDGET(4),
    CUSTOM_APPWIDGET(5),
    DEEP_SHORTCUT(6)
}