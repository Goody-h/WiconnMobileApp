package com.orsteg.wiconnmobileapp

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_status.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class StatusActivity : AppCompatActivity() {


    val FULLSCREEN = View.SYSTEM_UI_FLAG_LAYOUT_STABLE +
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION +
            View.SYSTEM_UI_FLAG_FULLSCREEN
    val FULLSCREEN_NO_NAV = View.SYSTEM_UI_FLAG_LAYOUT_STABLE +
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION +
            View.SYSTEM_UI_FLAG_FULLSCREEN + View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    var isKeyboardOpen = false
    var mIsPanelOpen = false

    var mNavBarUtil: NavBarUtil? = null

    var mStoryTransformer: StoryTransformer? = null


    private var mContentTouch: ContentTouchListener = ContentTouchListener()

    private var mUiVisibilityManager: UiVisibilityManager? = null

    var emojiKeyboard: EmojIconActions? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the initial UI Visibility
        mUiVisibilityManager = UiVisibilityManager(window.decorView)

        setContentView(R.layout.activity_status)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emojiKeyboard = EmojIconActions(this, window.decorView.rootView, message, emoji)
        emojiKeyboard?.setIconsIds(R.drawable.ic_keyboard_black_24dp, R.drawable.ic_insert_emoticon_black_24dp)
        emojiKeyboard?.ShowEmojIcon()

        mStoryTransformer = StoryTransformer()

        fab.setOnClickListener { _ ->

        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 0) {
                    if (cam.visibility == View.VISIBLE) {
                        cam.visibility = View.GONE
                    }
                } else {
                    if (cam.visibility == View.GONE) {
                        cam.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }

        reply.setOnClickListener { v ->
            if (!isKeyboardOpen) {
                showInput()
            }
        }

        scrim.setOnClickListener {
            hideInput()
        }

        message.addTextChangedListener(watcher)

        container.offscreenPageLimit = 2
        container.adapter = SectionsPagerAdapter(supportFragmentManager)
        container.currentItem = 4

        mStoryTransformer?.apply {
            container.setPageTransformer(false, this)
            container.addOnPageChangeListener(this)
        }

        mNavBarUtil = NavBarUtil(container, reply_wrapper)

        appBarFix()

        content_wrapper.setOnTouchListener(mContentTouch)

    }

    private inner class UiVisibilityManager(val decorView: View) {
        var mCurrentVisibility : Int = FULLSCREEN
        var mResetTask : TimerTask? = null
        var isSystemCalled : Boolean = false

        init {
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                isSystemCalled = visibility != FULLSCREEN && visibility != FULLSCREEN_NO_NAV
                if (isSystemCalled){
                    mResetTask?.cancel()
                    mResetTask = Timer().schedule(3000){
                        runOnUiThread {
                            isSystemCalled = false
                            window.decorView.systemUiVisibility = mCurrentVisibility
                        }
                    }
                }
            }
            setVisibility(mCurrentVisibility)
        }

        fun setVisibility(visibility: Int) {
            mCurrentVisibility = visibility
            if (!isSystemCalled) {
                decorView.systemUiVisibility = mCurrentVisibility
            }
        }
    }

    private inner class ContentTouchListener: View.OnTouchListener {

        var mFadeNavTask: TimerTask? = null

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
                progress.pauseGroup()

                mFadeNavTask = Timer().schedule(1000){
                    runOnUiThread {
                        appbar.visibility = View.GONE
                        reply_wrapper.visibility = View.GONE

                        mUiVisibilityManager?.setVisibility(FULLSCREEN_NO_NAV)
                    }
                }

            } else if (event?.actionMasked == MotionEvent.ACTION_UP || event?.actionMasked == MotionEvent.ACTION_CANCEL){

                mFadeNavTask?.cancel()

                appbar.visibility = View.VISIBLE
                reply_wrapper.visibility = View.VISIBLE

                mUiVisibilityManager?.setVisibility(FULLSCREEN)

                progress.playGroup()

            }
            content.dispatchTouchEvent(event)

            return true
        }

    }


    fun getFragment(position: Int) : StatusFragment? {
        return supportFragmentManager.findFragmentByTag(getFragmentName(position)) as? StatusFragment
    }

    private fun getFragmentName(id: Int): String {
        return "android:switcher:${container.id}:$id"
    }

    fun nextStatusGroup() {
        val next = container.currentItem + 1
        if (next < container.adapter?.count?:0) {
            container.setCurrentItem(next, true)
        } else {
            finish()
        }
    }


    fun previousStatusGroup() {
        val previous = container.currentItem - 1
        if (previous >= 0) {
            container.setCurrentItem(previous, true)
        } else {
            finish()
        }
    }

    class NavBarUtil(fullView: View, fittedView: View) {

        private var fullRight: Int? = 0
        private var fullLeft: Int? = 0
        private var fullBottom: Int? = 0
        private var fittedRight: Int? = 0
        private var fittedLeft: Int? = 0
        private var fittedBottom: Int? = 0

        private var isReady = false

        private var mNavWidth: Int = 0
        private var mNavHeight: Int = 0
        private var mNavLocation: Location = Location.NEUTRAL
        private var subscribers:  HashMap<String, (width: Int, height: Int, location: Location) -> Unit> = HashMap()

        init {

            fittedView.addOnLayoutChangeListener { _, l, _, r, b, _, _, _, _ ->
                if (fittedRight != r || fittedLeft != l || fittedBottom != b) {
                    fittedRight = r
                    fittedLeft = l
                    fittedBottom = b
                    if (fullRight != null) callSubscribers()
                }
            }

            fullView.addOnLayoutChangeListener { _, l, _, r, b, _, _, _, _ ->
                if (fullRight != r || fullLeft != l || fullBottom != b) {
                    fullRight = r
                    fullLeft = l
                    fullBottom = b
                    if (fittedRight != null) callSubscribers()
                }
            }
        }

        private fun callSubscribers() {
            if (fullRight == fittedRight && fullLeft == fittedLeft) {
                mNavWidth = 0
                mNavHeight = (fullBottom?:0) - (fittedBottom?:0)
                        mNavLocation = Location.NEUTRAL
            } else if (fullRight != fittedRight) {
                mNavWidth = (fullRight?:0) - (fittedRight?:0)
                mNavLocation = Location.RIGHT
                mNavHeight = 0
            } else if (fullLeft != fittedLeft) {
                mNavWidth = (fittedLeft?:0) - (fullLeft?:0)
                mNavLocation = Location.LEFT
                mNavHeight = 0
            }

            isReady = true
            subscribers.values.forEach { callback -> callback(mNavWidth, mNavHeight, mNavLocation) }
        }

        fun subscribeToNavWidth(tag: String, callback: (width: Int, height: Int, location: Location) -> Unit) {
            unSubscribeFromNavWidth(tag)
            subscribers.put(tag, callback)
            if (isReady) callback(mNavWidth, mNavHeight, mNavLocation)
        }

        fun getNavWidth(callback: (width: Int, height: Int, location: Location) -> Unit) : Boolean {
            if (isReady) callback(mNavWidth, mNavHeight, mNavLocation)
            return isReady
        }

        fun unSubscribeFromNavWidth(tag: String) {
            if (subscribers.containsKey(tag)) {
                subscribers.remove(tag)
            }
        }
        enum class Location {
            RIGHT, LEFT, NEUTRAL
        }
    }

    private fun appBarFix() {
        mNavBarUtil?.subscribeToNavWidth("appbar") {width, _, location ->
            when (location) {
                NavBarUtil.Location.NEUTRAL -> appbar.setPadding(0, 0, 0, 0)
                NavBarUtil.Location.RIGHT -> appbar.setPadding(0, 0, width, 0)
                NavBarUtil.Location.LEFT -> appbar.setPadding( width, 0, 0, 0)
            }
        }
    }

    override fun onBackPressed() {

        if (isKeyboardOpen) {
         hideInput(false)
        } else super.onBackPressed()
    }

    private fun hideInput(hideKeyboard: Boolean = true) {
        bottom.visibility = View.GONE
        scrim.visibility = View.GONE

        if (hideKeyboard){
            val inp = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            inp?.hideSoftInputFromWindow(message.windowToken, 0)
        }

        content_wrapper.setOnTouchListener(mContentTouch)
        progress.playGroup()


        fore.visibility =  View.VISIBLE
        //comment.visibility = View.VISIBLE

        isKeyboardOpen = false
    }

    private fun showInput() {
        content_wrapper.setOnTouchListener(null)
        progress.pauseGroup()

        fore.visibility =  View.GONE
        //comment.visibility = View.INVISIBLE
        bottom.visibility = View.VISIBLE
        scrim.visibility = View.VISIBLE

        message.requestFocus()
        val inp = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        inp?.showSoftInput(message, 0)

        isKeyboardOpen = true
    }

    override fun onPause() {
        super.onPause()
        progress.pauseGroup()
    }

    override fun onResume() {
        super.onResume()
        if (!isKeyboardOpen && !mIsPanelOpen) progress.playGroup()
    }

    inner class StoryTransformer : ViewPager.SimpleOnPageChangeListener(), ViewPager.PageTransformer {

        var mState: Int = ViewPager.SCROLL_STATE_IDLE

        override fun onPageScrollStateChanged(state: Int) {
            mState = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            if (positionOffset != 0f && mState == ViewPager.SCROLL_STATE_DRAGGING) {
                progress.pauseGroup()
                Log.d("mytag", "transform pauseAutoSeek $position")
            } else {
                progress.playGroup()
            }
            val alpha = when {
                positionOffset == 0f -> {
                    1f
                }
                positionOffset <= 0.25f -> {
                    //layout position
                    getFragment(position)?.mStatusGroupHolder?.showGroupLayout()
                    Log.d("mytag", "layout position")

                    1f - positionOffset/ 0.25f
                }
                positionOffset < 0.5f -> {

                    0f
                }
                positionOffset == 0.5f -> {

                    0f
                }
                else -> {
                    //layout position + 1
                    getFragment(position + 1)?.mStatusGroupHolder?.showGroupLayout()
                    Log.d("mytag", "layout position + 1")

                    (positionOffset - 0.5f) / 0.5f
                }
            }

            appbar.alpha = alpha
            content_wrapper.alpha = alpha

        }

        override fun onPageSelected(position: Int) {
            getFragment(position)?.mStatusGroupHolder?.setAsCurrentGroup()
            progress.playGroup()
        }

        override fun transformPage(page: View, position: Float) {
            page.apply {
                val maxAngle = 30f
                val sign = if (position < 0) -1 else 1
                val absPos = Math.abs(position)

                val angle = (maxAngle * absPos)
                val rad = Math.toRadians(angle.toDouble())

                val l = width.toDouble()
                val h = height.toDouble()

                val ax = Math.atan(l/h)
                val slope = Math.sqrt(h * h + l * l) / 2f

                val x = slope * Math.sin(ax + rad)  -  l / 2f
                val y = (h * (1f - Math.cos(rad))) / 2f

                rotation = angle * sign
                translationY = y.toFloat()
                translationX = x.toFloat() * sign

                val index = tag as Int

                if (position == 0f && (mState == ViewPager.SCROLL_STATE_IDLE || mState == ViewPager.SCROLL_STATE_SETTLING)) {
                    getFragment(index)?.mStatusGroupHolder?.setAsCurrentGroup()
                    progress.playGroup()
                    Log.d("mytag", "transform startAutoSeek $index")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.status_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_mute -> true
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPanelClosed(featureId: Int, menu: Menu?) {
        super.onPanelClosed(featureId, menu)
        mIsPanelOpen = false
        progress.playGroup()
    }

    override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        mIsPanelOpen = true
        progress.pauseGroup()
        return super.onMenuOpened(featureId, menu)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FriendRequestFragment (defined as a static inner class below).
            return StatusFragment.newInstance(position)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 10
        }
    }

    fun getProgressView() = progress

}
