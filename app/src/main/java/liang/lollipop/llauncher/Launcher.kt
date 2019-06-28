package liang.lollipop.llauncher

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import liang.lollipop.launcherbase.BaseActivity

/**
 * 启动器的主Activity
 * @date: 2018/12/04 22:14
 * @author: lollipop
 */
class Launcher : BaseActivity() {

    private var isOverview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val attributes = window.attributes
        attributes.systemUiVisibility = (
                    attributes.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = 0
        window.navigationBarColor = 0

        initView()
    }

    private fun initView() {
        overviewBtn.setOnClickListener {
            isOverview = !isOverview
            pagedGroup.setOverviewMode(isOverview)
        }
        onTouchSwitch.setOnCheckedChangeListener { _, isChecked ->
            pagedGroup.onePointMode = isChecked
        }
    }

}
