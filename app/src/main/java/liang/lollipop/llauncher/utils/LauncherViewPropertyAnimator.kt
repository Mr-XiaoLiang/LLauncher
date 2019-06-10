package liang.lollipop.llauncher.utils

import android.animation.Animator
import android.view.View
import android.view.ViewPropertyAnimator

/**
 * @date: 2019-06-08 20:38
 * @author: lollipop
 *
 */
class LauncherViewPropertyAnimator private constructor(private val view: View)
    : Animator.AnimatorListener {

    companion object {
        fun with(view: View): LauncherViewPropertyAnimator {
            return LauncherViewPropertyAnimator(view)
        }
    }

    private val listeners = ArrayList<Animator.AnimatorListener>()

    private var firstFrameHelper: FirstFrameAnimatorHelper? = null

    fun set(run: ViewPropertyAnimator.() -> Unit):LauncherViewPropertyAnimator {
        run(view.animate())
        return this
    }

    fun start():LauncherViewPropertyAnimator {
        view.animate().setListener(this).start()
        return this
    }

    fun on(listener: Animator.AnimatorListener):LauncherViewPropertyAnimator {
        listeners.add(listener)
        return this
    }

    override fun onAnimationRepeat(animation: Animator?) {
        listeners.forEach { it.onAnimationRepeat(animation) }
    }

    override fun onAnimationEnd(animation: Animator?) {
        listeners.forEach { it.onAnimationEnd(animation) }
    }

    override fun onAnimationCancel(animation: Animator?) {
        listeners.forEach { it.onAnimationCancel(animation) }
    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation != null) {
            firstFrameHelper?.onAnimationStart(animation)
        }
        listeners.forEach { it.onAnimationCancel(animation) }
    }

}