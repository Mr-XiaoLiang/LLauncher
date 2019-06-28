package liang.lollipop.llauncher.constant

import android.net.Uri
import android.provider.BaseColumns

/**
 * @date: 2019-06-11 22:43
 * @author: lollipop
 * 一些偏好设置项
 */
object Favorites {

    /**
     * 表名
     */
    const val TABLE_NAME = "favorites"

    /**
     * 最后修改时间
     * <P>Type: LONG</P>
     */
    const val MODIFIED = "modified"
    /**
     * 当前行的 ID
     * <P>Type: LONG</P>
     */
    const val _ID = BaseColumns._ID
    /**
     * 文件夹中包含的子项数量
     * <P>Type: LONG</P>
     */
    const val _COUNT = BaseColumns._COUNT

    /**
     * 用于展示的名称
     * <P>Type: TEXT</P>
     */
    const val TITLE = "title"

    /**
     * 操作允许触发的 intent，通过 [android.content.Intent.parseUri] 来生成。
     * 可以通过这个 intent 启动一些东西
     * <P>Type: TEXT</P>
     */
    const val INTENT = "intent"

    /**
     * 当前行的类型
     * <P>Type: INTEGER</P>
     */
    const val ITEM_TYPE = "itemType"

    /**
     * Intent.ShortcutIconResource 的 icon 包名
     * <P>Type: TEXT</P>
     */
    const val ICON_PACKAGE = "iconPackage"

    /**
     * Intent.ShortcutIconResource 的 icon 资源
     * <P>Type: TEXT</P>
     */
    const val ICON_RESOURCE = "iconResource"

    /**
     * icon 的位图（bitmap）
     * <P>Type: BLOB</P>
     */
    const val ICON = "icon"

    /**
     * 自定义 icon 的位图（bitmap）
     * <P>Type: BLOB</P>
     */
    const val CUSTOM_ICON = "customIcon"

    /**
     * 容器的 id
     * <P>Type: INTEGER</P>
     */
    const val CONTAINER = "container"

    /**
     * 所在屏幕的 id
     * <P>Type: INTEGER</P>
     */
    const val SCREEN = "screen"

    /**
     * 屏幕中的位置：X
     * <P>Type: INTEGER</P>
     */
    const val CELLX = "cellX"

    /**
     * 屏幕中的位置：Y
     * <P>Type: INTEGER</P>
     */
    const val CELLY = "cellY"

    /**
     * 横向的占用空间
     * <P>Type: INTEGER</P>
     */
    const val SPANX = "spanX"

    /**
     * 纵向占用的空间
     * <P>Type: INTEGER</P>
     */
    const val SPANY = "spanY"

    /**
     * 偏好设置配置文件的对应 id
     * Type: INTEGER
     */
    const val PROFILE_ID = "profileId"

    /**
     * 小部件的 id
     * <P>Type: INTEGER</P>
     */
    val APPWIDGET_ID = "appWidgetId"

    /**
     * 小部件程序提供的描述内容
     * <P>Type: STRING</P>
     */
    val APPWIDGET_PROVIDER = "appWidgetProvider"

    /**
     * 布尔值，表示他的项目已恢复但尚未成功绑定。
     * <P>Type: INTEGER</P>
     */
    val RESTORED = "restored"

    /**
     * 指示项目在自动排列的视图（如文件夹或hotseat）中的位置
     * <P>Type: INTEGER</P>
     */
    val RANK = "rank"

    /**
     * Stores general flag based options for [ItemInfo]s.
     * <P>Type: INTEGER</P>
     */
    val OPTIONS = "options"

    val TITLE_ALIAS = "titleAlias"



    val CONTENT_URI = Uri.parse("content://${ProviderConfig.AUTHORITY}/$TABLE_NAME")

    fun contentUri(id: Long): Uri {
        return Uri.parse("content://${ProviderConfig.AUTHORITY}/$TABLE_NAME/$id")
    }
}