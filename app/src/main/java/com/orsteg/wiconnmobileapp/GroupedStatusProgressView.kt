package com.orsteg.wiconnmobileapp

import android.content.Context
import android.util.AttributeSet

/**
 * Created by goodhope on 2/3/19.
 */
class GroupedStatusProgressView : StatusProgressView {

    private var mStatusGroup: StatusGroup? = null
    private var mIsGroupPaused: Boolean = false


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int): super(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)


    fun setCurrentStatusGroup(group: StatusGroup) {
        if (group != mStatusGroup) {
            pauseGroup()
            mStatusGroup?.resetGroup()
            mStatusGroup = group
            setOnStatusChangeListener(group as OnStatusChangeListener)
        }
    }

    fun pauseGroup() {
        if (!mIsGroupPaused) {
            mIsGroupPaused = true
            mStatusGroup?.pause()
        }
    }

    fun playGroup() {
        if (mIsGroupPaused) {
            mIsGroupPaused = false
            mStatusGroup?.play()
        }
    }

    fun getIsGroupPaused() : Boolean = mIsGroupPaused


    override fun startAutoSeek() {
        val isSeeking = getIsSeeking()

        super.startAutoSeek()

        if (!isSeeking) {
            mIsGroupPaused = false
        }
    }

    fun setGroupSeekTime(time: Long, noPause: Boolean = false) {
        if (!noPause) {
            pauseGroup()
        } else {
            mIsGroupPaused = false
        }
        setSeekTime(time)
    }

    abstract class StatusGroup : OnStatusChangeListener {
        abstract fun showGroupLayout()
        abstract fun setAsCurrentGroup()
        abstract fun resetGroup()
        abstract fun pause()
        abstract fun play()
    }

}