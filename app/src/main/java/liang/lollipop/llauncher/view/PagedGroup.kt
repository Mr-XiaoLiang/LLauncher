package liang.lollipop.llauncher.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Scroller
import liang.lollipop.llauncher.R
import liang.lollipop.llauncher.utils.*

/**
 * @date: 2019-05-01 22:15
 * @author: lollipop
 * Launcher的分页滑动的ViewGroup
 * 这是一个容器，用于排列每一个 Page
 */
open class PagedGroup(context: Context, attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    ViewGroup(context, attr, defStyleAttr, defStyleRes), ViewGroup.OnHierarchyChangeListener {

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int): this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)
    constructor(context: Context): this(context, null)

    companion object {
        protected const val INVALID_POINTER = -1
        protected const val INVALID_PAGE = -1
        private const val FLING_THRESHOLD_VELOCITY = 500
        private const val MIN_SNAP_VELOCITY = 1500
        private const val MIN_FLING_VELOCITY = 250
        private const val REORDERING_DROP_REPOSITION_DURATION = 200L
        private const val NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT = 2
        private const val PAGE_SNAP_ANIMATION_DURATION = 750
        private const val REORDERING_REORDER_REPOSITION_DURATION = 300L
        private const val REORDERING_SIDE_PAGE_HOVER_TIMEOUT = 80L
        // 如果触摸时，页面滑动了一半以上，那么松手时自动滑动到下一页
        private const val SIGNIFICANT_MOVE_THRESHOLD = 0.4f
        // 用于注册的最小拖动距离，以防止随机页面移位
        private const val MIN_LENGTH_FOR_FLING = 25
        private const val RETURN_TO_ORIGINAL_PAGE_THRESHOLD = 0.33f
        // 缩小的动画时间
        private const val ZOOM_IN_DURATION = 200L

        /**
         * 用于监听器的最小误差值
         */
        private const val OFFSET_MIN_DEVIATION = 0.00001F
    }

    /**
     * 最小缩放比例
     * 将对最大的尺寸做乘法
     */
    private var minScale = 0.8F

    /**
     * 是否从右到左排列
     */
    private val isRtl = resources.isRtl

    /**
     * 每个 Page 的坐标点
     */
    private var pageScrolls = IntArray(0)

    /**
     * Page之间的间隔
     */
    private var pageSpacing = resources.dp(10F).toInt()

    /**
     * 是否是概览模式
     */
    private var isOverview = false

    /**
     * 概览模式下的偏移量，
     * 即上空白所占全部空白的的比例
     * 如果值为 0.5F 那么说明上下空白一致
     */
    private var overviewOffsetY = 0.25F

    /**
     * 窗口的大小
     */
    private val pageSize = Rect()

    /**
     * 单指模式
     * 只响应最后一个手指的事件
     * 防止误触，参考 iOS 桌面
     */
    var onePointMode = false

    /**
     * 滑动的范围
     */
    private var scrollRange = 0

    /**
     * 速度计算器
     */
    private var velocityTracker: VelocityTracker? = null

    /**
     * 按下位置的坐标点
     */
    private val downPoint = PointF()

    /**
     * 按下屏幕时的滑动位置
     */
    private var downScrollX = 0

    /**
     * 上次手势的位置
     */
    private var lastMotion = PointF()

    /**
     * 每次滑动剩下的差值
     * 由于 scrollTo 方法只接受 int 类型，
     * 所以作为浮点数据的手势数据，会存在一些误差
     * 这里累计和保留差值，累加到下次的滑动中，
     * 减少误差
     */
    private var lastMotionXRemainder = 0F

    /**
     * 当前手指的 id
     */
    private var activePointerId = INVALID_POINTER

    /**
     * 总的 X 轴滑动距离
     */
    private var totalMotionX = 0F

    /**
     * 手指状态
     */
    private var touchState = TouchState.REST

    /**
     * 手势的触发阈值
     */
    private val touchSlop: Int

    /**
     * 最大的速度值
     */
    private val maximumVelocity: Int

    /**
     * 最小的惯性滑动速度
     */
    private val minFlingVelocity: Int

    /**
     * 启动惯性滑动的阈值
     */
    private val flingThresholdVelocity: Int

    /**
     * 最小快照速度
     */
    private val minSnapVelocity: Int

    /**
     * 页面是否正在滑动
     */
    private var isPageMoving = false

    /**
     * 当前选中的页面
     */
    private var currentPage = INVALID_PAGE

    /**
     * 下一个页面
     */
    private var nextPage = INVALID_PAGE

    /**
     * 滑动辅助器
     */
    private val scroller = Scroller(context)

    /**
     *
     */
    private var freeScroll = false

    /**
     * 取消点击事件
     */
    private var cancelTap = false

    /**
     * 被拖拽的 View
     */
    private var dragView: View? = null

    /**
     * 是否正在拖拽中
     */
    private var isReordering = false

    /**
     * 临时用的，可见页面的范围
     */
    private val tempVisiblePagesRange = IntArray(2)

    /**
     * 是否开始了拖拽
     */
    private var reorderingStarted = false

    /**
     * 拖拽的 View 的左侧边距
     */
    private var dragViewBaselineLeft = 0F

    /**
     * 拖拽事件结束前的任务
     */
    private var postReorderingPreZoomInRunnable: Runnable? = null

    /**
     * 用于计算拖拽事件动画次数的计数器
     */
    private var postReorderingPreZoomInRemainingAnimationCount = 0

    /**
     * 滑动时的最小值和最大值
     */
    private var freeScrollMinScrollX = -1
    private var freeScrollMaxScrollX = -1

    /**
     * 允许过度滑动
     */
    private var allowOverScroll = true

    /**
     * 强制启动滑动
     */
    private var forceScreenScrolled = false

    private var unboundedScrollX: Int = 0

    /**
     * 拖拽页面时的目标
     */
    private var sidePageHoverIndex = -1

    /**
     * 拖拽的目标的操作任务
     */
    private var sidePageHoverRunnable: Runnable? = null

    /**
     * 滑动的监听器
     */
    private val onScrollChangeListeners = ArrayList<OnScrollChangeListener>()

    /**
     * 是否在缩放模式，如果是的话，那么将会放弃所有手势
     */
    private var isInZoom = false

    /**
     * 缩放用的辅助动画类
     */
    private var zoomHelper = ZoomHelper().apply {
        animationDuration = ZOOM_IN_DURATION
        zoomOutValue = 1F
        zoomInValue = 0F
        zoomProgress = 1F
    }.onEnd {
        onOverviewAnimationEnd()
    }.onStart {
        onOverviewAnimationStart()
    }.onUpdate {
        onOverviewAnimationUpdate(it)
    }

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledPagingTouchSlop
        maximumVelocity = configuration.scaledMaximumFlingVelocity

        val density = resources.displayMetrics.density
        flingThresholdVelocity = (FLING_THRESHOLD_VELOCITY * density).toInt()
        minFlingVelocity = (MIN_FLING_VELOCITY * density).toInt()
        minSnapVelocity = (MIN_SNAP_VELOCITY * density).toInt()
        initListener()
    }

    private fun initListener() {
        setOnHierarchyChangeListener(this)
        setWillNotDraw(false)
    }

    private fun acquireVelocityTrackerAndAddMovement(ev: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(ev)
    }

    private fun releaseVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker?.clear()
            velocityTracker?.recycle()
            velocityTracker = null
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) {
            return super.onInterceptTouchEvent(ev)
        }
        acquireVelocityTrackerAndAddMovement(ev)
        if (childCount < 1) {
            return super.onInterceptTouchEvent(ev)
        }

        // 如果是移动事件，并且正在滑动状态下，那么直接拦截事件
        if (ev.action == MotionEvent.ACTION_MOVE && touchState == TouchState.SCROLLING) {
            return true
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId != INVALID_POINTER) {
                    determineScrollingStart(ev)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                // 记录按下时的位置信息
                downPoint.set(x, y)
                downScrollX = scrollX
                lastMotion.set(x, y)
                lastMotionXRemainder = 0F
                totalMotionX = 0F
                activePointerId = ev.getPointerId(0)

                val xDist = Math.abs(scroller.finalX - scroller.currX)
                val finishedScrolling = (scroller.isFinished || xDist < touchSlop / 3)
                if (finishedScrolling) {
                    touchState = TouchState.REST
                    if (!scroller.isFinished && !freeScroll) {
                        pageEndMoving()
                    }
                } else {
                    touchState = if (isTouchPointInViewport(downPoint.x.toInt(), downPoint.y.toInt())) {
                        TouchState.SCROLLING
                    } else {
                        TouchState.REST
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetTouchState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                releaseVelocityTracker()
            }
        }

        return touchState != TouchState.REST
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            if (onePointMode) {
                // 如果是单指头模式，那么在活跃的指头抬起时放弃所有手势
                onTouchUp(ev)
                return
            }
            // 当活跃的那个指头抬起，那么重新选定一个指头作为事件来源
            val newPointerIndex = if (pointerIndex == 0) {1} else {0}
            downPoint.set(ev.getX(newPointerIndex), ev.getY(newPointerIndex))
            lastMotion.set(downPoint)
            lastMotionXRemainder = 0f
            activePointerId = ev.getPointerId(newPointerIndex)
            velocityTracker?.clear()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (childCount < 1 || event == null || isInZoom) {
            return super.onTouchEvent(event)
        }

        acquireVelocityTrackerAndAddMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished) {
                    abortScrollerAnimation(false)
                }
                // 记录初次按下的位置信息
                downPoint.set(event.x, event.y)
                lastMotion.set(downPoint)
                downScrollX = scrollX
                lastMotionXRemainder = 0F
                totalMotionX = 0F
                activePointerId = event.getPointerId(0)
                if (touchState == TouchState.SCROLLING) {
                    onScrollBegin()
                    pageBeginMoving()
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (onePointMode) {
                    val actionIndex = event.actionIndex
                    activePointerId = event.getPointerId(actionIndex)
                    downPoint.set(event.getX(actionIndex), event.getY(actionIndex))
                    lastMotion.set(downPoint)
                    lastMotionXRemainder = 0F
                }
            }
            MotionEvent.ACTION_MOVE -> {
                return onTouchMove(event)
            }
            MotionEvent.ACTION_UP -> {
                return onTouchUp(event)
            }
            MotionEvent.ACTION_CANCEL -> {
                if (touchState == TouchState.SCROLLING) {
                    snapToPage(getPageNearestToCenterOfScreen())
                    onScrollEnd()
                }
                resetTouchState()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                releaseVelocityTracker()
            }
        }
        return true
    }

    private fun onTouchMove(event: MotionEvent): Boolean {
        when (touchState) {
            TouchState.SCROLLING -> {
                val pointIndex = event.findPointerIndex(activePointerId)

                if (pointIndex < 0) {
                    return true
                }

                val x = event.getX(pointIndex)
                val deltaX = lastMotion.x + lastMotionXRemainder - x
                totalMotionX += Math.abs(deltaX)

                if (Math.abs(deltaX) >= 1.0F) {
                    scrollBy(deltaX.toInt(), 0)
                    lastMotion.x = x
                    lastMotionXRemainder = deltaX - deltaX.toInt()
                } else {
                    awakenScrollBars()
                }
            }
            TouchState.REORDERING -> {
                // 更新手指的位置信息
                lastMotion.set(event.x, event.y)
                updateDragViewTranslationDuringDrag()

                // 获取到被拖拽的 View 的位置
                val dragViewIndex = indexOfChild(dragView)

                val pageUnderPointIndex = getNearestHoverOverPageIndex()
                val underPage = getPageAt(pageUnderPointIndex)
                if (pageUnderPointIndex != dragViewIndex
                    && underPage is Page && underPage.canMove()) {

                    tempVisiblePagesRange[0] = 0
                    tempVisiblePagesRange[1] = pageCount - 1
                    getFreeScrollPageRange(tempVisiblePagesRange)
                    if (tempVisiblePagesRange[0] <= pageUnderPointIndex &&
                            pageUnderPointIndex <= tempVisiblePagesRange[1] &&
                            pageUnderPointIndex != sidePageHoverIndex) {
                        sidePageHoverIndex = pageUnderPointIndex
                        sidePageHoverRunnable = Runnable {
                            snapToPage(pageUnderPointIndex)
                            val shiftDelta = if (dragViewIndex < pageUnderPointIndex) {
                                -1
                            } else {
                                1
                            }
                            val lowerIndex = if (dragViewIndex < pageUnderPointIndex) {
                                dragViewIndex + 1
                            } else {
                                pageUnderPointIndex
                            }
                            val upperIndex = if (dragViewIndex > pageUnderPointIndex){
                                dragViewIndex - 1
                            } else {
                                pageUnderPointIndex
                            }
                            for (i in lowerIndex..upperIndex) {
                                val v = getChildAt(i)

                                val oldX = v.left
                                val newX = getChildAt(i + shiftDelta).left

                                var anim: ObjectAnimator? = v.tag as ObjectAnimator
                                anim?.cancel()

                                v.translationX = (oldX - newX).toFloat()
                                anim = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0F)
                                FirstFrameAnimatorHelper(anim!!, v)
                                anim.duration = REORDERING_REORDER_REPOSITION_DURATION
                                anim.start()
                                v.tag = anim
                            }

                            removeView(dragView)
                            addView(dragView, pageUnderPointIndex)
                            sidePageHoverIndex = -1
                            updatePageIndicator()
                        }
                        postDelayed(sidePageHoverRunnable, REORDERING_SIDE_PAGE_HOVER_TIMEOUT)
                    }
                } else {
                    removeCallbacks(sidePageHoverRunnable)
                    sidePageHoverIndex = -1
                }
            }
            else -> {
                determineScrollingStart(event)
            }
        }
        return true
    }

    private fun onTouchUp(event: MotionEvent): Boolean {
        when (touchState) {
            TouchState.SCROLLING -> {
                val pointIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointIndex)
                velocityTracker?.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val velocityX = velocityTracker?.getXVelocity(activePointerId)?:0F
                val deltaX = (x - downPoint.x).toInt()
                val pageWidth = getPageAt(currentPage)?.measuredWidth?:0
                val isSignificantMove = Math.abs(deltaX) > pageWidth * SIGNIFICANT_MOVE_THRESHOLD

                totalMotionX += Math.abs(lastMotion.x + lastMotionXRemainder - x)

                val isFling = totalMotionX > MIN_LENGTH_FOR_FLING && Math.abs(velocityX) > minFlingVelocity

                if (!freeScroll) {
                    // 如果页面向一个方向移动然后向相反方向移动，
                    // 我们使用阈值来确定是否应该返回到起始页面，
                    // 或者是否应该再跳过一个页面。
                    val returnToOriginalPage = (Math.abs(deltaX) > pageWidth * RETURN_TO_ORIGINAL_PAGE_THRESHOLD &&
                            Math.signum(velocityX) != Math.signum(deltaX.toFloat()) && isFling)

                    val isDeltaXLeft = if (isRtl) { deltaX > 0 } else { deltaX < 0 }
                    val isVelocityXLeft = if (isRtl) { velocityX > 0 } else { velocityX < 0 }
                    if ((isSignificantMove && !isDeltaXLeft && !isFling || isFling && !isVelocityXLeft) && currentPage > 0) {
                        val finalPage = if (returnToOriginalPage) currentPage else currentPage - 1
                        snapToPageWithVelocity(finalPage, velocityX.toInt())
                    } else if ((isSignificantMove && isDeltaXLeft && !isFling || isFling && isVelocityXLeft) &&
                        currentPage < childCount - 1) {
                        val finalPage = if (returnToOriginalPage) { currentPage } else { currentPage + 1 }
                        snapToPageWithVelocity(finalPage, velocityX.toInt())
                    } else {
                        snapToPage()
                    }
                } else {
                    if (!scroller.isFinished) {
                        val scaleX = if (isOverview) { minScale } else { 1F }
                        abortScrollerAnimation(true)
                        val vX = (-velocityX * scaleX).toInt()
                        val initialScrollX = (scrollX * scaleX).toInt()
                        scroller.fling(initialScrollX, 0, vX, 0,
                            Int.MIN_VALUE, Int.MAX_VALUE, 0, 0)
                        nextPage = getPageNearestToCenterOfScreen((scroller.finalX / scaleX).toInt())
                        invalidate()
                    }
                }
                onScrollEnd()
            }
            TouchState.PREV_PAGE -> {
                val nextPage = Math.max(0, currentPage - 1)
                if (nextPage != currentPage) {
                    snapToPage(nextPage)
                } else {
                    snapToPage()
                }
            }
            TouchState.NEXT_PAGE -> {
                val nextPage = Math.min(childCount - 1, currentPage + 1)
                if (nextPage != currentPage) {
                    snapToPage(nextPage)
                } else {
                    snapToPage()
                }
            }
            TouchState.REORDERING -> {
                // 更新最后一次手势的位置
                lastMotion.set(event.x, event.y)
                updateDragViewTranslationDuringDrag()
            }
            else -> {
                snapToPage()
                if (!cancelTap) {
                    callOnClick()
                }
            }
        }
        removeCallbacks(sidePageHoverRunnable)
        resetTouchState()
        return true
    }

    private fun getNearestHoverOverPageIndex(): Int {
        dragView?.let {
            val dragX = (it.left.toFloat() + (it.measuredWidth / 2).toFloat()
                    + it.translationX).toInt()
            getFreeScrollPageRange(tempVisiblePagesRange)
            var minDistance = Integer.MAX_VALUE
            var minIndex = indexOfChild(it)
            for (i in tempVisiblePagesRange[0]..tempVisiblePagesRange[1]) {
                getPageAt(i)?.let { page ->
                    val pageX = page.left + page.measuredWidth / 2
                    val d = Math.abs(dragX - pageX)
                    if (d < minDistance) {
                        minIndex = i
                        minDistance = d
                    }
                }
            }
            return minIndex
        }?:return -1
    }

    private fun updateDragViewTranslationDuringDrag() {
        dragView?.let {
            val x = lastMotion.x - downPoint.x + (scrollX - downScrollX) +
                    (dragViewBaselineLeft - it.left)
            // val y = lastMotion.y - downPoint.y
            it.translationX = x
            // it.translationY = y
        }
    }

    private fun resetTouchState() {
        releaseVelocityTracker()
        endReordering()
        cancelTap = false
        touchState = TouchState.REST
        activePointerId = INVALID_POINTER
    }

    // Animate the drag view back to the original position
    private fun animateDragViewToOriginalPosition() {
        dragView?.let {
            LauncherViewPropertyAnimator.with(it).set {
                translationX = 0F
                translationY = 0F
                scaleX = 1F
                scaleY = 1F
                duration = REORDERING_DROP_REPOSITION_DURATION
            }.on(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onPostReorderingAnimationCompleted()
                }
            }).start()
        }
    }

    private fun onStartReordering() {
        // 设置为拖拽状态，将会允许滑动删除和拖拽排序
        touchState = TouchState.REORDERING
        isReordering = true

        // 更新图层显示
        invalidate()
    }

    private fun onPostReorderingAnimationCompleted() {
        // 拖拽操作完成后的回调方法
        --postReorderingPreZoomInRemainingAnimationCount
        if (postReorderingPreZoomInRemainingAnimationCount == 0) {
            postReorderingPreZoomInRunnable?.run()
            postReorderingPreZoomInRunnable = null
        }
    }

    private fun onEndReordering() {
        isReordering = false
    }

    fun startReordering(v: View): Boolean {
        val dragViewIndex = indexOfChild(v)

        // 如果不是闲置状态，或者不允许移动，那么放弃它
        if (touchState != TouchState.REST) return false

        if (v !is Page || !v.canMove()) {
            return false
        }

        tempVisiblePagesRange[0] = 0
        tempVisiblePagesRange[1] = pageCount - 1
        getFreeScrollPageRange(tempVisiblePagesRange)
        reorderingStarted = true

        // 检查被排序的 View 是否是在排序的 View 范围内
        if (tempVisiblePagesRange[0] <= dragViewIndex && dragViewIndex <= tempVisiblePagesRange[1]) {
            // 找到相应坐标的 view
            dragView = getChildAt(dragViewIndex)
            dragView?.apply {
                animate().scaleX(1.15f).scaleY(1.15f).setDuration(100).start()
            }
            dragViewBaselineLeft = dragView?.left?.toFloat()?:0F
            snapToPage(getPageNearestToCenterOfScreen())
            disableFreeScroll()
            onStartReordering()
            return true
        }
        return false
    }

    /**
     * 我们希望页面捕捉动画的持续时间受到屏幕必须移动的距离的影响，
     * 但是，我们不希望这种持续时间以纯线性方式实现。
     * 相反，我们使用此方法来缓和行程距离对整体捕捉持续时间的影响。
     */
    private fun distanceInfluenceForSnapDuration(float: Float): Float {
        var f = float
        f -= 0.5f // 将值集中在0左右。
        f *= (0.3f * Math.PI / 2.0f).toFloat()
        return Math.sin(f.toDouble()).toFloat()
    }

    private fun snapToPageWithVelocity(which: Int, velo: Int) {
        var whichPage = which
        var velocity = velo
        whichPage = validateNewPage(whichPage)
        val halfScreenSize = pageWidth / 2

        val newX = getScrollForPage(whichPage)
        val delta = newX - unboundedScrollX

        if (Math.abs(velocity) < minFlingVelocity) {
            // 如果速度足够低，那么将其视为自动页面推进而不是对投掷的明显物理响应
            snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION)
            return
        }

        // 在这里，我们计算将用于计算整体捕捉持续时间的“距离”。 这是需要行走的实际距离的函数;
        // 我们将此值保持接近半屏尺寸，以减少捕捉持续时间的变化，作为页面需要移动的距离的函数。
        val distanceRatio = Math.min(1f, 1.0f * Math.abs(delta) / (2 * halfScreenSize))
        val distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration(distanceRatio)

        velocity = Math.abs(velocity)
        velocity = Math.max(minSnapVelocity, velocity)

        // 我们希望页面的捕捉速度大致与用户挥动的速度相匹配，
        // 因此我们将持续时间缩放到接近滚动插值器的导数的值为零
        val duration = 4 * Math.round(1000 * Math.abs(distance / velocity))

        snapToPage(whichPage, delta, duration)
    }

    private fun snapToPage() {
        snapToPage(getPageNearestToCenterOfScreen())
    }

    private fun snapToPage(whichPage: Int) {
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION)
    }

    private fun snapToPage(whichPage: Int, duration: Int) {
        snapToPage(whichPage, duration, false)
    }

    private fun snapToPage(which: Int, duration: Int, immediate: Boolean) {
        var whichPage = which
        whichPage = validateNewPage(whichPage)

        val newX = getScrollForPage(whichPage)
        val delta = newX - unboundedScrollX
        snapToPage(whichPage, delta, duration, immediate)
    }

    private fun snapToPage(whichPage: Int, delta: Int, duration: Int) {
        snapToPage(whichPage, delta, duration, false)
    }

    private fun snapToPage(which: Int, delta: Int, d: Int, immediate: Boolean = false) {
        var whichPage = which
        var duration = d
        whichPage = validateNewPage(whichPage)

        nextPage = whichPage

        pageBeginMoving()
        awakenScrollBars(duration)
        if (immediate) {
            duration = 0
        } else if (duration == 0) {
            duration = Math.abs(delta)
        }

        if (!scroller.isFinished) {
            abortScrollerAnimation(false)
        }

        scroller.startScroll(unboundedScrollX, 0, delta, 0, duration)

        updatePageIndicator()

        // 按需要确定是否触发完成滑动的方法
        if (immediate) {
            computeScroll()
        }

        forceScreenScrolled = true
        invalidate()
    }

    override fun scrollTo(xValue: Int, y: Int) {
        var x = xValue
        if (freeScroll) {
            if (!scroller.isFinished && (x > freeScrollMaxScrollX || x < freeScrollMinScrollX)) {
                forceFinishScroller(false)
            }

            x = x.boundToRange(freeScrollMinScrollX, freeScrollMaxScrollX)
        }

        unboundedScrollX = x
        super.scrollTo(x, y)
        if (isReordering(true)) {
            updateDragViewTranslationDuringDrag()
        }
    }

    // 将函数独立开来，是为了能更简单的复用它
    private fun computeScrollHelper(): Boolean {
        return computeScrollHelper(true)
    }

    private fun computeScrollHelper(shouldInvalidate: Boolean): Boolean {
        if (scroller.computeScrollOffset()) {
            // 如果不需要移动，那么就不触发滚动
            if (scrollX != scroller.currX || scrollY != scroller.currY) {
                var scaleX = if (isOverview) { minScale } else { 1F }
                scaleX = if (freeScroll) { scaleX } else { 1f }
                val scrollX = (scroller.currX * (1 / scaleX)).toInt()
                scrollTo(scrollX, scroller.currY)
            }
            if (shouldInvalidate) {
                invalidate()
            }
            return true
        } else if (nextPage != INVALID_PAGE && shouldInvalidate) {
            sendScrollAccessibilityEvent()

            currentPage = validateNewPage(nextPage)
            nextPage = INVALID_PAGE
            notifyPageSwitchListener()

            // 除非页面已经确定且用户已停止滚动，否则我们不希望触发页面结束移动
            if (touchState == TouchState.REST) {
                pageEndMoving()
            }

            onPostReorderingAnimationCompleted()
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            if (am.isEnabled) {
                // Notify the user when the page changes
                announceForAccessibility(getCurrentPageDescription())
            }
            return true
        }
        return false
    }

    private fun getCurrentPageDescription(): String {
        return context.getString(R.string.default_scroll_format,
            getNextPage() + 1, childCount
        )
    }

    private fun sendScrollAccessibilityEvent() {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (am.isEnabled) {
            if (currentPage != getNextPage()) {
                val ev = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED)
                ev.isScrollable = true
                ev.scrollX = scrollX
                ev.scrollY = scrollY
                ev.maxScrollX = maxScrollX
                ev.maxScrollY = 0

                sendAccessibilityEventUnchecked(ev)
            }
        }
    }

    override fun computeScroll() {
        computeScrollHelper()
    }

    private fun getPageNearestToCenterOfScreen(): Int {
        return getPageNearestToCenterOfScreen(scrollX)
    }

    private fun getPageNearestToCenterOfScreen(scaledScrollX: Int): Int {
        val screenCenter = scaledScrollX + pageWidth / 2
        var minDistanceFromScreenCenter = Integer.MAX_VALUE
        var minDistanceFromScreenCenterIndex = -1
        val childCount = childCount
        for (i in 0 until childCount) {
            val layout = getPageAt(i)?:continue
            val childWidth = layout.measuredWidth
            val halfChildWidth = childWidth / 2
            val childCenter = layout.left + halfChildWidth
            val distanceFromScreenCenter = Math.abs(childCenter - screenCenter)
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter
                minDistanceFromScreenCenterIndex = i
            }
        }
        return minDistanceFromScreenCenterIndex
    }

    private fun isReordering(testTouchState: Boolean): Boolean {
        var state = isReordering
        if (testTouchState) {
            state = state and (touchState == TouchState.REORDERING)
        }
        return state
    }

    private fun endReordering() {
        // 为了简化逻辑，即使没有开始拖动，也会调用endReordering方法
        // 这时，放弃操作
        if (!reorderingStarted) {
            return
        }
        reorderingStarted = false

        // 使各个 View 归位
        val onCompleteRunnable = Runnable { onEndReordering() }

        postReorderingPreZoomInRunnable = Runnable {
            onCompleteRunnable.run()
            enableFreeScroll()
        }

        postReorderingPreZoomInRemainingAnimationCount = NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT
        // 滑动到当前拖拽到 View 的位置
        snapToPage(indexOfChild(dragView), 0)
        // 将拖拽的 View 放到原来的位置
        animateDragViewToOriginalPosition()
    }

    private fun enableFreeScroll() {
        setEnableFreeScroll(true)
    }

    private fun disableFreeScroll() {
        setEnableFreeScroll(false)
    }

    private fun setEnableFreeScroll(freeScroll: Boolean) {
        val wasFreeScroll = freeScroll
        this.freeScroll = freeScroll

        if (freeScroll) {
            updateFreeScrollBounds()
            getFreeScrollPageRange(tempVisiblePagesRange)
            if (currentPage < tempVisiblePagesRange[0]) {
                changeCurrentPage(tempVisiblePagesRange[0])
            } else if (currentPage > tempVisiblePagesRange[1]) {
                changeCurrentPage(tempVisiblePagesRange[1])
            }
        } else if (wasFreeScroll) {
            snapToPage(getNextPage())
        }

        setEnableOverScroll(!freeScroll)
    }

    private fun abortScrollerAnimation(resetNextPage: Boolean) {
        scroller.abortAnimation()
        if (resetNextPage) {
            nextPage = INVALID_PAGE
        }
    }

    private fun updateFreeScrollBounds() {
        getFreeScrollPageRange(tempVisiblePagesRange)
        if (isRtl) {
            freeScrollMinScrollX = getScrollForPage(tempVisiblePagesRange[1])
            freeScrollMaxScrollX = getScrollForPage(tempVisiblePagesRange[0])
        } else {
            freeScrollMinScrollX = getScrollForPage(tempVisiblePagesRange[0])
            freeScrollMaxScrollX = getScrollForPage(tempVisiblePagesRange[1])
        }
    }

    private fun setEnableOverScroll(enable: Boolean) {
        allowOverScroll = enable
    }

    private fun getScrollForPage(index: Int): Int {
        return if (pageScrolls.isEmpty() || index >= pageScrolls.size || index < 0) {
            0
        } else {
            pageScrolls[index]
        }
    }

    private fun getFreeScrollPageRange(range: IntArray) {
        range[0] = 0
        range[1] = Math.max(0, childCount - 1)
    }

    /**
     * 确认是否开始滑动
     */
    private fun determineScrollingStart(ev: MotionEvent, touchSlopScale: Float = 1F) {
        val pointerIndex = ev.findPointerIndex(activePointerId)
        if (pointerIndex < 0) {
            return
        }
        // 只获取指定手指的事件，以此应对多指场景
        val x = ev.getX(pointerIndex)
        val y = ev.getY(pointerIndex)

        // 如果不是在 View 操作范围内的话，放弃事件
        if (!isTouchPointInViewport(x.toInt(), y.toInt())) {
            return
        }

        val xDiff = Math.abs(x - lastMotion.x)
        val ts = Math.round(touchSlopScale * touchSlop)
        val xMoved = xDiff > ts

        if (xMoved) {
            // 如果用户 X 轴方向移动距离超过了阈值，那么开始滑动
            // 切换状态
            touchState = TouchState.SCROLLING
            // 记录总的滑动距离
            totalMotionX += Math.abs(lastMotion.x - x)
            // 记录本次的手指位置
            lastMotion.x = x
            // 置空差值
            lastMotionXRemainder = 0F
            // 回调启动滑动的预备方法
            onScrollBegin()
            // 发起页面滑动回调方法
            pageBeginMoving()
            // 取消子 View 的长按事件
            requestDisallowInterceptTouchEvent(true)
        }

    }

    protected open fun onScrollBegin() {}

    protected open fun onScrollEnd() {}

    private fun pageBeginMoving() {
        if (!isPageMoving) {
            isPageMoving = true
            onPageBeginMoving()
        }
    }

    protected open fun onPageBeginMoving() {}

    private fun pageEndMoving() {
        if (!isPageMoving) {
            isPageMoving = false
            onPageEndMoving()
        }
    }

    protected open fun onPageEndMoving() {}

    private fun isTouchPointInViewport(x: Int, y: Int): Boolean {
        return pageSize.contains(x, y)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            getPageAt(currentPage)?.cancelLongPress()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    enum class TouchState(val value: Int) {
        /**  闲置 **/
        REST(0),
        /**  滑动中 **/
        SCROLLING(1),
        /**  上一页 **/
        PREV_PAGE(2),
        /**  下一页 **/
        NEXT_PAGE(3),
        /**  排序中 **/
        REORDERING(4)
    }

    override fun computeHorizontalScrollRange(): Int {
        return scrollRange
    }

    override fun onChildViewRemoved(parent: View?, child: View?) {
        invalidate()
    }

    override fun onChildViewAdded(parent: View?, child: View?) {
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount < 1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 如果尺寸不能明确，那么放弃测量
        if (widthSize < 1 || widthMode == MeasureSpec.UNSPECIFIED ||
                heightSize < 1 || heightMode == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        // 禁用 Padding，并且放弃对 inset 的处理，专注于对分页的排版
        pageSize.set(0, 0, widthSize, heightSize)

        // 循环测量每一个子 view
        measureChildren(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY))
        setMeasuredDimension(widthSize, heightSize)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount < 1) {
            return
        }
        // 更新得到新的页面尺寸
        val childWidth = r - l
        val childHeight = b - t

        // 更新当前的页面尺寸
        pageSize.set(0, 0, childWidth, childHeight)

        // 如果 page 的数量变化了，那么创建新的坐标数组
        if (childCount != pageScrolls.size) {
            pageScrolls = IntArray(childCount)
        }

        relayoutChildren()
        if (currentPage == INVALID_PAGE) {
            changeCurrentPage(0)
        }
    }

    private fun relayoutChildren() {
        if (isOverview) {
            layoutByOverview()
        } else {
            layoutByFull()
        }
    }

    private fun layoutByOverview() {
        val childWidth = overviewWidth
        val offsetPadding = (pageWidth - childWidth) / 2
        var left = offsetPadding
        val stepWidth = childWidth + pageSpacing
        val offsetY = overviewTop - (pageHeight - overviewHeight) / 2
        whileForLayout{index, child ->
            pageScrolls[index] = left
            child.layout(left, offsetY, left + pageWidth, offsetY + pageHeight)
            child.scaleX = minScale
            child.scaleY = minScale
            child.translationX = 0F
            child.translationY = 0F
            left += stepWidth
        }
        scrollRange = left - pageSpacing
    }

    private fun layoutByFull() {
        val childWidth = pageWidth
        val childHeight = pageHeight
        var left = 0
        val top = 0
        whileForLayout{index, child ->
            pageScrolls[index] = left
            child.layout(left, top, left + childWidth, top + childHeight)
            child.scaleX = 1F
            child.scaleY = 1F
            child.translationX = 0F
            child.translationY = 0F
            left += childWidth
        }
        scrollRange = left
    }

    private fun whileForLayout(run: (Int, View) -> Unit) {
        // 为了适应从右到左的排版要求，这里对 index 做一些兼容
        val startIndex = if (isRtl) { childCount - 1 } else { 0 }
        val endIndex = if (isRtl) { -1 } else { childCount }
        val delta = if (isRtl) { -1 } else { 1 }

        // 启动一个循环，遍历每一个子 view
        var index = startIndex
        while (index != endIndex) {
            val child = getChildAt(index)
            if (child.visibility == View.GONE) {
                continue
            }
            run(index, child)
            index += delta
        }
    }

    private val pageWidth: Int
        get() {
            return pageSize.width()
        }

    private val pageHeight: Int
        get() {
            return pageSize.height()
        }

    private val overviewHeight: Int
        get() {
            return (pageHeight * minScale).toInt()
        }

    private val overviewWidth: Int
        get() {
            return (pageWidth * minScale).toInt()
        }

    private val overviewTop: Int
        get() {
            return ((pageHeight - overviewHeight) * overviewOffsetY).toInt()
        }

    private val maxScrollX: Int
        get() {
            val childCount = childCount
            return if (childCount > 0) {
                val index = if (isRtl) 0 else childCount - 1
                getScrollForPage(index)
            } else {
                0
            }
    }

    private fun getPageAt(index: Int): View? {
        if (index < 0 || index >= childCount) {
            return null
        }
        return getChildAt(index)
    }

    private val pageCount: Int
        get() {
            return childCount
        }

    private fun getNextPage(): Int {
        return if (nextPage != INVALID_PAGE) {
            nextPage
        } else {
            currentPage
        }
    }

    private fun changeCurrentPage(page: Int) {
        if (!scroller.isFinished) {
            abortScrollerAnimation(true)
        }
        // don't introduce any checks like mCurrentPage == currentPage here-- if we change the
        // the default
        if (childCount == 0) {
            return
        }
        forceScreenScrolled = true
        currentPage = validateNewPage(page)
        updateCurrentPageScroll()
        notifyPageSwitchListener()
        invalidate()
    }

    private fun updateCurrentPageScroll() {
        // If the current page is invalid, just reset the scroll position to zero
        var newX = 0
        if (currentPage in 0 until pageCount) {
            newX = getScrollForPage(currentPage)
        }
        scrollTo(newX, 0)
        scroller.finalX = newX
        forceFinishScroller(true)
    }

    private fun forceFinishScroller(resetNextPage: Boolean) {
        scroller.forceFinished(true)
        // We need to clean up the next page here to avoid computeScrollHelper from
        // updating current page on the pass.
        if (resetNextPage) {
            nextPage = INVALID_PAGE
        }
    }

    private fun notifyPageSwitchListener() {
        onScrollChange()
        updatePageIndicator()
    }

    private fun updatePageIndicator() {
        // 更新页面指示器
        // TODO 更新页面指示器
    }

    private fun validateNewPage(newPage: Int): Int {
        var validatedPage = newPage
        // 如果是freeScroll模式，那么需要限制 index 的范围
        if (freeScroll) {
            getFreeScrollPageRange(tempVisiblePagesRange)
            validatedPage = Math.max(
                tempVisiblePagesRange[0],
                Math.min(newPage, tempVisiblePagesRange[1])
            )
        }
        // 保证最终的结果是在允许的范围内
        validatedPage = validatedPage.boundToRange(0, pageCount - 1)
        return validatedPage
    }

    private fun onScrollChange() {
        var pageIndex = currentPage
        var offsetPixels = pageScrolls[pageIndex] - scrollX
        val width = if (isOverview) {
            pageWidth * minScale + pageSpacing
        } else {
            pageWidth.toFloat()
        }
        var offset = offsetPixels * 1F / width

        // 如果最后的结果是 -1.0 或者 1.0，那么就将他重置为 0.0
        if (Math.abs(Math.abs(offset) - 1) < OFFSET_MIN_DEVIATION) {
            if (offset < 0) {
                pageIndex ++
            } else {
                pageIndex --
            }
            offsetPixels = 0
            offset = 0F
        }
        zoomHelper.selectedPage = pageIndex
        onScrollChangeListeners.forEach {
            it.onScrollChange(pageIndex, offset, offsetPixels)
        }
    }

    interface OnScrollChangeListener {
        fun onScrollChange(pageIndex: Int, offset: Float, offsetPixels: Int)
    }

    fun setOverviewMode(type: Boolean) {
        if (isOverview == type) {
            return
        }
        isOverview = type
        if (!scroller.isFinished) {
            abortScrollerAnimation(true)
        }
        zoomHelper.start(isOverview)
    }

    private fun onOverviewAnimationStart() {
        isInZoom = true
    }

    private fun onOverviewAnimationEnd() {
        isInZoom = false
        for(i in 0 until childCount) {
            getPageAt(i)?.let {
                it.translationX = 0F
                it.translationY = 0F
            }
        }
        relayoutChildren()
        scrollX = pageScrolls[zoomHelper.selectedPage]
    }

    private fun onOverviewAnimationUpdate(value: Float) {
        if (childCount < 1) {
            return
        }
        val selectedIndex = zoomHelper.selectedPage
        val lastIndex = (selectedIndex - 1).boundToRange(0, childCount - 1)
        val nextIndex = (selectedIndex + 1).boundToRange(0, childCount - 1)
        val scale = (1 - minScale) * value + minScale
        val offsetWidth = pageWidth - overviewWidth
        val progress = if (isOverview) {
            1 - value
        } else {
            value * - 1
        }
        val translationX = offsetWidth * progress
        val offsetY = overviewTop - (pageHeight - overviewHeight) / 2
        val translationY = offsetY * progress
        getPageAt(selectedIndex)?.let {
            it.scaleX = scale
            it.scaleY = scale
            it.translationY = translationY
        }
        if (lastIndex != selectedIndex) {
            getPageAt(lastIndex)?.let {
                it.scaleX = scale
                it.scaleY = scale
                var offsetX = translationX - (pageSpacing * progress)
                if (isRtl) {
                    offsetX *= -1
                }
                it.translationX = offsetX
                it.translationY = translationY
            }
        }
        if (nextIndex != selectedIndex) {
            getPageAt(nextIndex)?.let {
                it.scaleX = scale
                it.scaleY = scale
                var offsetX = (pageSpacing * progress) - translationX
                if (isRtl) {
                    offsetX *= -1
                }
                it.translationX = offsetX
                it.translationY = translationY
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        zoomHelper.end()
        return super.onSaveInstanceState()
    }

    class ZoomHelper: ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private val zoomAnimator = ValueAnimator().apply {
            addUpdateListener(this@ZoomHelper)
            addListener(this@ZoomHelper)
        }

        /**
         * 缩放子 View 的时候的进度值
         * 1F: 表示完全展开的状态
         * 0F: 表示完全缩小后的状态
         */
        var zoomProgress = 1F
        var animationDuration = 0L
        private var onUpdateListener: ((Float) -> Unit)? = null
        private var onStartListener: (() -> Unit)? = null
        private var onEndListener: (() -> Unit)? = null
        var zoomOutValue = 1F
        var zoomInValue = 0F

        var selectedPage = 0

        fun onUpdate(listener: (Float) -> Unit): ZoomHelper {
            onUpdateListener = listener
            return this
        }

        fun onStart(listener: () -> Unit): ZoomHelper {
            onStartListener = listener
            return this
        }

        fun onEnd(listener: () -> Unit): ZoomHelper {
            onEndListener = listener
            return this
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            if (animation == zoomAnimator) {
                zoomProgress = animation.animatedValue as Float
                onUpdateListener?.let {
                    it(zoomProgress)
                }
            }
        }

        override fun onAnimationRepeat(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            onEndListener?.let {
                it()
            }
        }

        override fun onAnimationCancel(animation: Animator?) {}

        override fun onAnimationStart(animation: Animator?) {
            onStartListener?.let {
                it()
            }
        }

        fun start(zoomIn: Boolean) {
            zoomAnimator.cancel()
            if (zoomIn) {
                zoomAnimator.setFloatValues(zoomProgress, zoomInValue)
                zoomAnimator.duration = (animationDuration * 1F * (zoomProgress - zoomInValue) / (zoomOutValue - zoomInValue)).toLong()
            } else {
                zoomAnimator.setFloatValues(zoomProgress, zoomOutValue)
                zoomAnimator.duration = (animationDuration * 1F * (zoomOutValue - zoomProgress) / (zoomOutValue - zoomInValue)).toLong()
            }
            zoomAnimator.start()
        }

        fun end() {
            zoomAnimator.end()
        }
    }

}