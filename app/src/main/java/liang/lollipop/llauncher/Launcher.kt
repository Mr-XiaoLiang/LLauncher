package liang.lollipop.llauncher

import android.os.Bundle
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
        overviewBtn.setOnClickListener {
            isOverview = !isOverview
            pagedGroup.setOverviewMode(isOverview)
        }
        onTouchSwitch.setOnCheckedChangeListener { _, isChecked ->
            pagedGroup.onePointMode = isChecked
        }
    }
}
