package com.orsteg.wiconnmobileapp

import android.os.Bundle
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by goodhope on 2/20/19.
 */
class AuthHandler(private var mState: Int = STATE_UNKNOWN, private var mListener: ((state: Int) -> Unit)?) {
    private val random = Random()
    private var authTask: TimerTask? = null

    init {

        mListener?.invoke(mState)

        if (mState == STATE_UNKNOWN) authTask = Timer().schedule(4000){
            var state = random.nextInt(1) + 1

            state = STATE_OFFLINE

            setAuthState(state)
        }

    }

    fun setAuthStateChangeListener(listener: (state: Int) -> Unit) {
        mListener = listener
    }

    fun clearAuthStateChangeListener() {
        mListener = null
    }

    fun saveState(outState: Bundle?) {
        outState?.putInt("auth_state", mState)
    }

    fun getAuthState() :Int = mState

    fun setAuthState(state: Int) {
        if (mState != state) {
            mState = state
            mListener?.invoke(state)
        }
    }

    companion object {
        val SIGN_UP_REQUEST = 44
        val STATE_UNKNOWN = 0
        val STATE_ONLINE = 1
        val STATE_OFFLINE = 2
        val STATE_OFFLINE_LOGIN = 3
    }
}