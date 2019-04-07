package com.orsteg.wiconnmobileapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Created by goodhope on 2/3/19.
 */
open class StatusProgressView : View {

    private var mStatusCount: Int = 0
    private var mViewCount: Int = 0
    private var mCurrentTotalSeekTime: Long = 5000
    private var mSeekTime: Long = 0
    private var mIsSeeking: Boolean = false
    private var mIsManualSeeking = false

    private var mSpace: Float = dpToPx(10f)
    private var mActivePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private var mInactivePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var valueSeeker: ValueAnimator = ValueAnimator().apply {
        interpolator = LinearInterpolator()
        setObjectValues(0, mCurrentTotalSeekTime)
        setEvaluator { fraction, startValue, endValue ->
            val diff = (endValue as Long) - (startValue as Long)
            (startValue + diff * fraction).toLong()
        }
    }
    private var backupSeeker: ValueAnimator = ValueAnimator().apply {
        interpolator = LinearInterpolator()
        setObjectValues(0, mCurrentTotalSeekTime)
        setEvaluator { fraction, startValue, endValue ->
            val diff = (endValue as Long) - (startValue as Long)
            (startValue + diff * fraction).toLong()
        }
    }
    private var backupTimer = BackupTimer()
    private var useBackup: Boolean = false

    private var mStatusChangeListener: OnStatusChangeListener? = null


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs, 0) {
        init(context, attrs, 0, 0)
    }
    constructor(context: Context, attrs: AttributeSet?,
                    defStyleAttr: Int): super(context, attrs, defStyleAttr, 0) {
        init(context, attrs, defStyleAttr, 0)
    }
    constructor(context: Context, attrs: AttributeSet?,
                    defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?,
             defStyleAttr: Int, defStyleRes: Int) {

        // Load the styled attributes and set their properties
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.StatusProgressView, defStyleAttr, defStyleRes)

        mStatusCount = attributes.getInteger(R.styleable.StatusProgressView_status_count, 1)
        mViewCount = attributes.getInteger(R.styleable.StatusProgressView_view_count, 0)

        val listen: (ValueAnimator) -> Unit = { listener ->

            val currSeek = listener.animatedValue as Long

            if (listener.currentPlayTime != listener.duration && currSeek == mCurrentTotalSeekTime) {
                backupTimer.start(mCurrentTotalSeekTime - mSeekTime)
            } else {
                mSeekTime = currSeek
                invalidate()

                notifySeekChange()

                if (listener.animatedFraction == 1f) {
                    getNextStatus(true)
                }
            }
        }
        valueSeeker.addUpdateListener { listener -> listen(listener)}
        backupSeeker.addUpdateListener { listener -> listen(listener)}

        setBarColors(attributes.getColor(R.styleable.StatusProgressView_active_color, Color.BLUE),
                attributes.getColor(R.styleable.StatusProgressView_inactive_color, Color.GRAY))

        attributes.recycle()
    }

    private fun dpToPx(valueInDp: Float) : Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val mWidth = width
        val mHeight = height
        var space = mSpace / (Math.floor((mStatusCount / 10.0)).toInt() + 1)
        if (mStatusCount < 2) space = 0f

        val length = if (mStatusCount > 0) {(mWidth - (mStatusCount - 1) * space) / mStatusCount}
                    else 0f

        mActivePaint.strokeWidth = mHeight.toFloat()
        mInactivePaint.strokeWidth = mHeight.toFloat()

        // draw progress
        for (i in 0 until mStatusCount) {

            val startX = (length + space) * i
            val endX = startX + length
            val mPaint = if (i < mViewCount) mActivePaint else mInactivePaint

            canvas?.drawLine(startX, mHeight/2f, endX, mHeight/2f, mPaint)

            if (i == mViewCount && mSeekTime > 0) {
                val end = startX + length * (mSeekTime.toFloat()/mCurrentTotalSeekTime.toFloat())
                canvas?.drawLine(startX, mHeight/2f, end, mHeight/2f, mActivePaint)
            }
        }
    }

    fun pauseAutoSeek() {
        if (valueSeeker.isRunning){
            valueSeeker.cancel()
            mStatusChangeListener?.onStateChange(mViewCount, STATE_PAUSED)
        } else if (backupSeeker.isRunning){
            backupSeeker.cancel()
            mStatusChangeListener?.onStateChange(mViewCount, STATE_PAUSED)
        }

        backupTimer.cancel()

        mIsSeeking = false
    }

    open fun startAutoSeek() {
        if (!mIsSeeking) {
            valueSeeker.apply {
                duration = mCurrentTotalSeekTime - mSeekTime
                setObjectValues(mSeekTime, mCurrentTotalSeekTime)
            }

            backupSeeker.apply {
                duration = mCurrentTotalSeekTime - mSeekTime
                setObjectValues(mSeekTime, mCurrentTotalSeekTime)
            }

            if (useBackup) {
                backupSeeker.start()
            } else {
                valueSeeker.start()
            }

            useBackup = !useBackup

            mStatusChangeListener?.onStateChange(mViewCount, STATE_PLAYING)
            mIsSeeking = true
            mIsManualSeeking = false
        }
    }

    fun getIsSeeking() : Boolean = mIsSeeking

    fun getCurrentTotalSeekTime() : Long = mCurrentTotalSeekTime

    fun getSeekTime() : Long = mSeekTime

    fun getIsManualSeeking() : Boolean = mIsManualSeeking

    fun setStatusCount(count: Int) {
        if (!mIsSeeking) {
            mStatusCount = count
            invalidate()
        }
    }

    fun getStatusCount() : Int = mStatusCount

    fun setViewCount(count: Int) {
        if (!mIsSeeking) {
            mViewCount = count
            invalidate()
        }
    }

    fun getViewCount() : Int = mViewCount

    fun getNextStatus(isComplete: Boolean = false) {
        pauseAutoSeek()
        mSeekTime = 0
        if (mViewCount < mStatusCount){
            mViewCount++

            notifySeekChange()

            mStatusChangeListener?.onStatusChange(mViewCount, isComplete)
        }

        mIsManualSeeking = false
        invalidate()
    }

    fun getPreviousStatus() {
        pauseAutoSeek()
        mSeekTime = 0

        val result = mViewCount - 1

        if (mViewCount > 0) {
            mViewCount--
        }

        mIsManualSeeking = false

        notifySeekChange(result)

        mStatusChangeListener?.onStatusChange(result, false)

        invalidate()
    }

    fun setBarColors(activeColor: Int, inactiveColor: Int) {
        mInactivePaint.color = inactiveColor
        mActivePaint.color = activeColor
        invalidate()
    }

    fun setActiveBarColor(activeColor: Int) {
        mActivePaint.color = activeColor
        invalidate()
    }

    fun getActiveBarColor() : Int = mActivePaint.color

    fun setInactiveBarColor(inactiveColor: Int) {
        mInactivePaint.color = inactiveColor
        invalidate()
    }

    fun getInactiveBarColor() : Int = mInactivePaint.color

    fun setOnStatusChangeListener(listener: OnStatusChangeListener) {
        mStatusChangeListener = listener
    }

    fun clearOnStatusStateListener() {
        mStatusChangeListener = null
    }

    fun setCurrentTotalSeekTime(time: Long) {
        pauseAutoSeek()
        mCurrentTotalSeekTime = time
        mIsManualSeeking = true
        invalidate()
    }

    protected fun notifySeekChange(statusIndex: Int = mViewCount) {
        mStatusChangeListener?.onStatusProgress(statusIndex, mSeekTime.toFloat()/mCurrentTotalSeekTime.toFloat(),
                mCurrentTotalSeekTime, mSeekTime)
    }

    fun setSeekTime(time: Long) {
        pauseAutoSeek()

        mSeekTime = time

        mIsManualSeeking = true
        invalidate()
        if (mSeekTime == mCurrentTotalSeekTime) {
            getNextStatus(true)
        }
    }

    private inner class BackupTimer {
        private var mTimer: CountDownTimer? = null
        private var isRunning: Boolean = false

        private fun getTimer(seekTime: Long): CountDownTimer = object : CountDownTimer(seekTime, 1) {
            override fun onFinish() {
                notifySeekChange()

                getNextStatus(true)
                isRunning = false
            }
            override fun onTick(millisUntilFinished: Long) {
                mSeekTime = mCurrentTotalSeekTime - millisUntilFinished
                invalidate()

                notifySeekChange()
            }
        }

        fun getIsRunning(): Boolean = isRunning

        fun start(seekTime: Long) {
            if (isRunning) cancel()
            mTimer = getTimer(seekTime)
            mTimer?.start()
            isRunning = true
        }

        fun cancel() {
            if (isRunning) {
                mTimer?.cancel()
                mStatusChangeListener?.onStateChange(mViewCount, STATE_PAUSED)
            }
            isRunning = false
        }
    }


    interface OnStatusChangeListener {
        fun onStatusChange(statusIndex: Int, completed: Boolean)
        fun onStatusProgress(statusIndex: Int, progressFraction: Float,
                             totalTime: Long, elapsedTime: Long)
        fun onStateChange(statusIndex: Int, state: Int)
    }

    companion object {
        val STATE_PAUSED = 0
        val STATE_PLAYING = 1
    }
}