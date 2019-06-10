package liang.lollipop.llauncher.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @date: 2019-05-02 00:17
 * @author: lollipop
 *
 */
class Workspace(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    FrameLayout(context, attr, defStyleAttr, defStyleRes) {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)



}