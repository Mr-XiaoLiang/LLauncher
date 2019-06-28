package liang.lollipop.llauncher.data

import android.content.ContentValues
import android.os.UserHandle
import liang.lollipop.llauncher.constant.ItemType

/**
 * @date: 2019-06-11 22:43
 * @author: lollipop
 * 用于表示桌面中的元素
 */
open class ItemInfo() {

    companion object {
        const val NO_ID = -1L

        const val CONTAINER_DESKTOP = -100L
        const val CONTAINER_HOTSEAT = -101L
    }

    /**
     * 当前元素在数据库中的 id
     */
    var id = NO_ID

    /**
     * 元素的类型，默认为应用程序
     */
    var itemType = ItemType.APPLICATION

    /**
     * 所在的容器，
     * 如果是在桌面，那么就是{@link #CONTAINER_DESKTOP},
     * 如果是在应用列表，那么就是{@link #NO_ID}，代表他没有被设置，
     * 如果是在某个文件夹，那么就是相应文件夹的 id
     */
    var container = NO_ID

    /**
     * 组件所在屏幕的 id
     */
    var screenId: Long = NO_ID

    /**
     * 组件的位置
     * 单位：格
     */
    var cellX = -1

    /**
     * 组件的位置
     * 单位：格
     */
    var cellY = -1

    /**
     * 组件的占用空间
     * 单位：格
     */
    var spanX = 1

    /**
     * 组件的占用空间
     * 单位：格
     */
    var spanY = 1

    /**
     * 组件的最小空间占用，
     * 单位：格
     */
    var minSpanX = 1

    /**
     * 组件的最小空间占用，
     * 单位：格
     */
    var minSpanY = 1

    /**
     * 应用在有序列表中的位置
     */
    var rank = 0

    /**
     * 组件的标题
     */
    var title: CharSequence = ""

    /**
     * 组件的描述内容
     */
    var contentDescription: CharSequence = ""

    /**
     * 所属用户
     */
    var user: UserHandle = android.os.Process.myUserHandle()

    constructor(info: ItemInfo): this() {
        copyFrom(info)
    }

    fun copyFrom(info: ItemInfo) {
        id = info.id
        cellX = info.cellX
        cellY = info.cellY
        spanX = info.spanX
        spanY = info.spanY
        rank = info.rank
        screenId = info.screenId
        itemType = info.itemType
        container = info.container
        user = info.user
        contentDescription = info.contentDescription
    }

    fun writeToValues(values: ContentValues) {
//        values.put(LauncherSettings.Favorites.ITEM_TYPE, itemType)
//        values.put(LauncherSettings.Favorites.CONTAINER, container)
//        values.put(LauncherSettings.Favorites.SCREEN, screenId)
//        values.put(LauncherSettings.Favorites.CELLX, cellX)
//        values.put(LauncherSettings.Favorites.CELLY, cellY)
//        values.put(LauncherSettings.Favorites.SPANX, spanX)
//        values.put(LauncherSettings.Favorites.SPANY, spanY)
//        values.put(LauncherSettings.Favorites.RANK, rank)
    }

    fun readFromValues(values: ContentValues) {
//        itemType = values.getAsInteger(LauncherSettings.Favorites.ITEM_TYPE)!!
//        container = values.getAsLong(LauncherSettings.Favorites.CONTAINER)!!
//        screenId = values.getAsLong(LauncherSettings.Favorites.SCREEN)!!
//        cellX = values.getAsInteger(LauncherSettings.Favorites.CELLX)!!
//        cellY = values.getAsInteger(LauncherSettings.Favorites.CELLY)!!
//        spanX = values.getAsInteger(LauncherSettings.Favorites.SPANX)!!
//        spanY = values.getAsInteger(LauncherSettings.Favorites.SPANY)!!
//        rank = values.getAsInteger(LauncherSettings.Favorites.RANK)!!
    }

}