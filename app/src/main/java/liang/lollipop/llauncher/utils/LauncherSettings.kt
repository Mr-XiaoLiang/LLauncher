package liang.lollipop.llauncher.utils

import android.provider.BaseColumns

/**
 * @date: 2019-06-11 22:43
 * @author: lollipop
 * 启动器中的一些设置
 */
object LauncherSettings {

    object BaseLauncherColumns {

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
         * The gesture is an application
         */
        const val ITEM_TYPE_APPLICATION = 0

        /**
         * The gesture is an application created shortcut
         */
        const val ITEM_TYPE_SHORTCUT = 1

        /**
         * The icon package name in Intent.ShortcutIconResource
         * <P>Type: TEXT</P>
         */
        const val ICON_PACKAGE = "iconPackage"

        /**
         * The icon resource name in Intent.ShortcutIconResource
         * <P>Type: TEXT</P>
         */
        const val ICON_RESOURCE = "iconResource"

        /**
         * The custom icon bitmap.
         * <P>Type: BLOB</P>
         */
        const val ICON = "icon"

        const val CUSTOM_ICON = "customIcon"
    }

}