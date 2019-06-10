package liang.lollipop.llauncher.utils


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver

/**
 * @date: 2019-06-08 20:29
 * @author: lollipop
 * 一个用于辅助动画的前 2 帧效果的辅助器
 * 用来避免前两帧的抖动
 */
class FirstFrameAnimatorHelper(private val targetView: View) : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {
    private var startFrame: Long = 0
    private var startTime: Long = -1
    private var handlingOnAnimationUpdate: Boolean = false
    private var adjustedSecondFrameTime: Boolean = false

    constructor(animator: ValueAnimator, targetView: View): this(targetView) {
        animator.addUpdateListener(this)
    }

    constructor(vpa: ViewPropertyAnimator, targetView: View): this(targetView) {
        vpa.setListener(this)
    }

    // only used for ViewPropertyAnimators
    override fun onAnimationStart(animation: Animator) {
        val va = animation as ValueAnimator
        va.addUpdateListener(this)
        onAnimationUpdate(va)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val currentTime = System.currentTimeMillis()
        if (startTime == -1L) {
            startFrame = sGlobalFrameCounter
            startTime = currentTime
        }

        val currentPlayTime = animation.currentPlayTime
        val isFinalFrame = java.lang.Float.compare(1f, animation.animatedFraction) == 0

        if (!handlingOnAnimationUpdate &&
            sVisible &&
            // If the current play time exceeds the duration, or the animated fraction is 1,
            // the animation will get finished, even if we call setCurrentPlayTime -- therefore
            // don't adjust the animation in that case
            currentPlayTime < animation.duration && !isFinalFrame
        ) {
            handlingOnAnimationUpdate = true
            val frameNum = sGlobalFrameCounter - startFrame
            // If we haven't drawn our first frame, reset the time to t = 0
            // (give up after MAX_DELAY ms of waiting though - might happen, for example, if we
            // are no longer in the foreground and no frames are being rendered ever)
            if (frameNum == 0L && currentTime < startTime + MAX_DELAY && currentPlayTime > 0) {
                // The first frame on animations doesn't always trigger an invalidate...
                // force an invalidate here to make sure the animation continues to advance
                targetView.rootView.invalidate()
                animation.currentPlayTime = 0
                // For the second frame, if the first frame took more than 16ms,
                // adjust the start time and pretend it took only 16ms anyway. This
                // prevents a large jump in the animation due to an expensive first frame
            } else if (frameNum == 1L && currentTime < startTime + MAX_DELAY &&
                !adjustedSecondFrameTime &&
                currentTime > startTime + IDEAL_FRAME_DURATION &&
                currentPlayTime > IDEAL_FRAME_DURATION
            ) {
                animation.currentPlayTime = IDEAL_FRAME_DURATION.toLong()
                adjustedSecondFrameTime = true
            } else if (frameNum > 1) {
                targetView.post { animation.removeUpdateListener(this@FirstFrameAnimatorHelper) }
            }
            handlingOnAnimationUpdate = false
        }
    }

    companion object {
        private const val MAX_DELAY = 1000
        private const val IDEAL_FRAME_DURATION = 16

        private var sGlobalDrawListener: ViewTreeObserver.OnDrawListener? = null
        internal var sGlobalFrameCounter: Long = 0
        private var sVisible: Boolean = false

        fun setIsVisible(visible: Boolean) {
            sVisible = visible
        }

        fun initializeDrawListener(view: View) {
            if (sGlobalDrawListener != null) {
                view.viewTreeObserver.removeOnDrawListener(sGlobalDrawListener)
            }
            sGlobalDrawListener = ViewTreeObserver.OnDrawListener { sGlobalFrameCounter++ }
            view.viewTreeObserver.addOnDrawListener(sGlobalDrawListener)
            sVisible = true
        }
    }

}