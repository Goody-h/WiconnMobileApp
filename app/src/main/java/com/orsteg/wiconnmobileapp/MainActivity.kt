package com.orsteg.wiconnmobileapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.design.widget.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.*
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_main.*
import kotlinx.android.synthetic.main.fragment_main2.view.*
import kotlinx.android.synthetic.main.stories_layout.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, CameraFragment.CameraInterface {
    override fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePic ->
            takePic.resolveActivity(packageManager)?.also {
                startActivityForResult(takePic, 109)
            }
        }
    }

    override fun closeGetMedia() {
        container.currentItem = 1
    }

    override fun openVideo() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takePic ->
            takePic.resolveActivity(packageManager)?.also {
                startActivityForResult(takePic, 107)
            }
        }
    }

    override fun openGallery() {
        val openIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        openIntent.addCategory(Intent.CATEGORY_OPENABLE)
        openIntent.type = "*/*"
        val types = arrayOf("image/*", "video/*")
        openIntent.putExtra(Intent.EXTRA_MIME_TYPES, types)
        startActivityForResult(Intent.createChooser(openIntent, "Select Media"), 105)
    }

    fun getMedia() {
        behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
        container.currentItem = 2
    }


    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private var mTransformer: CameraTransform? = null

    private var mScrimController: ScrimController? = null

    private var mFabController: FabStateController? = null

    private var behaviour: BottomSheetBehavior<View>? = null

    private var mStatusManager: StatusManager? = null

    private var mAuthHandler: AuthHandler? = null

    private var currentFragment: Fragment? = null


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.AppTheme_NoActionBar_NoSplash)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        if (savedInstanceState != null) {

        }

        mTransformer = CameraTransform()

        mFabController = FabStateController(this, fab)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.

        container.adapter = mSectionsPagerAdapter
        container.setPageTransformer(false, mTransformer)
        mTransformer?.apply {
            container.addOnPageChangeListener(this)
        }

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        setUpTabs()


        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {

                when(position) {
                    2 -> {
                        fab.visibility = View.GONE
                        stories.visibility = View.GONE
                    }
                    1 -> {
                        stories.visibility = View.VISIBLE
                        fab.visibility = View.VISIBLE

                        mFabController?.setState(FabStateController.FabState(R.drawable.ic_person_white_24dp){
                            val i = Intent(this@MainActivity, NewChatActivity::class.java)
                            startActivity(i)
                        })
                    }
                    0 -> {
                        stories.visibility = View.VISIBLE
                        fab.visibility = View.VISIBLE

                        mFabController?.setState(FabStateController.FabState(R.drawable.ic_phone_in_talk_black_24dp){
                            val i = Intent(this@MainActivity, NewCallActivity::class.java)
                            startActivity(i)
                        })
                    }

                }
            }
        }
        )

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        if (savedInstanceState == null) container.currentItem = 1

        nav_view.getHeaderView(0)?.findViewById<View?>(R.id.edit)?.setOnClickListener {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
        }

        mScrimController = ScrimController(content_scrim)

        setStoriesView()


        splash.setOnClickListener {  }

        mAuthHandler = AuthHandler(savedInstanceState
                ?.getInt("auth_state", AuthHandler.STATE_UNKNOWN)
                ?:AuthHandler.STATE_UNKNOWN) { authState ->

            when(authState) {
                AuthHandler.STATE_UNKNOWN -> {
                    runOnUiThread {
                        startLoader()
                    }
                }
                AuthHandler.STATE_ONLINE -> {
                    runOnUiThread {
                        init()
                    }
                }
                AuthHandler.STATE_OFFLINE -> {
                    runOnUiThread {
                        setOfflineView()
                    }
                }
                AuthHandler.STATE_OFFLINE_LOGIN -> {
                    runOnUiThread {
                        openLogin()
                    }
                }
            }
        }
    }


    fun startLogin() {
        mAuthHandler?.setAuthState(AuthHandler.STATE_OFFLINE_LOGIN)
    }

    fun openLogin() {
        splash.visibility = View.VISIBLE

        var isNull = false
        val fragment = supportFragmentManager.findFragmentByTag("fragment.login")
                ?: LoginFragment.newInstance("", "").apply { isNull = true }

        if (fragment != currentFragment) {

            val transaction = supportFragmentManager.beginTransaction()

            val auth = supportFragmentManager.findFragmentByTag("fragment.auth")

            if (auth != null && auth.isVisible) {
                (auth as AuthFragment).addSharedElements(transaction)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.apply {
                    sharedElementEnterTransition = DetailsTransition().apply {
                        duration = 400
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                    enterTransition = Slide().apply {
                        duration = 600
                        addTarget(R.id.fields)
                        interpolator = AccelerateDecelerateInterpolator()
                        slideEdge = Gravity.END
                    }
                }
            }

            if (isNull) {
                transaction.replace(splash.id, fragment, "fragment.login")
            } else {
                transaction.replace(splash.id, fragment)
            }

            transaction.disallowAddToBackStack()

            transaction.commitAllowingStateLoss()
        }
        currentFragment = fragment

    }

    fun hideInput(){
        val v = this.currentFocus
        if (v != null){
            val inp = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inp?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun login() {
        mAuthHandler?.setAuthState(AuthHandler.STATE_ONLINE)
    }

    fun logout() {
        mAuthHandler?.setAuthState(AuthHandler.STATE_OFFLINE)
    }

    fun signUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivityForResult(intent, AuthHandler.SIGN_UP_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AuthHandler.SIGN_UP_REQUEST) {
                login()
            }
        }
    }

    private fun setOfflineView() {

        splash.visibility = View.VISIBLE

        var isNull = false
        val fragment = supportFragmentManager.findFragmentByTag("fragment.auth")
                ?: AuthFragment.newInstance("", "").apply { isNull = true }

        if (fragment != currentFragment) {

            val transaction = supportFragmentManager.beginTransaction()

            val load = supportFragmentManager.findFragmentByTag("fragment.load")

            if (load != null && load.isVisible) {
                transaction.addSharedElement((load as LoadFragment).getLogo(), "app_logo")
            } else {
                val login = supportFragmentManager.findFragmentByTag("fragment.login")

                if (login != null && login.isVisible) {
                    (login as LoginFragment).addSharedElements(transaction)
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.apply {
                    sharedElementEnterTransition = DetailsTransition().apply {
                        duration = 600
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                    enterTransition = Explode().apply {
                        duration = 600
                        addTarget(R.id.options)
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                }
            }

            if (isNull) {
                transaction.replace(splash.id, fragment, "fragment.auth")
            } else {
                transaction.replace(splash.id, fragment)
            }

            transaction.disallowAddToBackStack()

            transaction.commitAllowingStateLoss()
        }
        currentFragment = fragment


        // disable user content and views if enabled
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class DetailsTransition: TransitionSet() {
        init {
            ordering = ORDERING_TOGETHER
            addTransition(ChangeBounds()).addTransition(ChangeTransform()).addTransition(ChangeImageTransform())
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mAuthHandler?.saveState(outState)

    }


    private fun startLoader() {

        splash.visibility = View.VISIBLE

        var isNull = false
        val fragment = supportFragmentManager.findFragmentByTag("fragment.load")
                ?: LoadFragment.newInstance("", "").apply { isNull = true }

        if (fragment != currentFragment) {

            val transaction = supportFragmentManager.beginTransaction()

            if (isNull) {
                transaction.replace(splash.id, fragment, "fragment.load")
            } else {
                transaction.replace(splash.id, fragment)
            }

            transaction.commit()
        }
        currentFragment = fragment
    }

    private fun init() {
        // clear the splash screen
        splash.visibility = View.GONE
        if (currentFragment != null && currentFragment?.isVisible == true) {
            supportFragmentManager.beginTransaction().remove(currentFragment).commit()
        }

        // carry out user initializations
    }


    /**
     * Custom behaviour for the stories bottom sheet
     */
    class MyAppBarBehaviour(context: Context, attr: AttributeSet) : AppBarLayout.Behavior(context, attr) {

        private var mIsSheetTouched: Boolean = false

        override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
            mIsSheetTouched = target.id == R.id.story

            return !mIsSheetTouched && super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
        }

        override fun onInterceptTouchEvent(parent: CoordinatorLayout?, child: AppBarLayout?, ev: MotionEvent?): Boolean {
            if (ev?.actionMasked == MotionEvent.ACTION_CANCEL) mIsSheetTouched = false

            return !mIsSheetTouched && super.onInterceptTouchEvent(parent, child, ev)
        }
    }

    private fun setStoriesView() {
        mStatusManager = StatusManager(this, story)

        behaviour = BottomSheetBehavior.from(stories)

        behaviour?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0) behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
                mScrimController?.setAppScrim(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

        })

        text_story.setOnClickListener {
            val intent = Intent(this@MainActivity, WriteStatusActivity::class.java)
            startActivity(intent)
        }

        media_story.setOnClickListener {
            getMedia()
        }

        layout.setOnClickListener {
            if (mStatusManager?.mLayoutStyle == 0) {
                mStatusManager?.useGridLayout()
                layout.setImageResource(R.drawable.ic_view_list_white_24dp)
            } else {
                mStatusManager?.useListLayout()
                layout.setImageResource(R.drawable.ic_view_quilt_white_24dp)
            }
        }
    }

    class FabStateController(context: Context, val fab: FloatingActionButton) {

        private var currentState: FabState? = null
        private var isInTemporaryState = false

        init {
            setState(FabStateController.FabState(R.drawable.ic_phone_in_talk_black_24dp){
                val i = Intent(context, NewCallActivity::class.java)
                context.startActivity(i)
            })
        }

        fun setState(state: FabState?) {
            currentState = state
            if (!isInTemporaryState)setFabAttributes(state)
        }

        private fun setFabAttributes(state: FabState?) {
            state?.apply {
                fab.setImageResource(resId)
                fab.setOnClickListener{ listener() }
            }?: fab.setOnClickListener(null)
        }

        class FabState (val resId: Int, val listener: () -> Unit)
    }

    class ScrimController(private val content: View) {

        private var maxAlpha = 0.75f

        fun setAppScrim(level: Float) : Float {

            val l = fixLevel(level)
            val alpha = maxAlpha * l

            content.alpha = alpha

            if (alpha <= 0f) {
                hideScrim()
            } else if (alpha > 0f) {
                content.visibility = View.VISIBLE
            }
            return alpha
        }

        private fun fixLevel(level: Float): Float {
            return when {
                level < 0f -> 0f
                level > 1f -> 1f
                else -> level
            }
        }

        private fun hideScrim() {
            content.visibility = View.GONE
            content.setOnClickListener(null)
        }
    }

    fun setUpTabs() {

        val aTabs = arrayOf(TabData(R.drawable.chat_tab, 0), TabData(R.drawable.call_tab, 20))
        for (i in 0..1) {
            val tab = tabs.newTab()
            val view = layoutInflater.inflate(R.layout.custom_tab, tabs, false)
            view.findViewById<ImageView?>(R.id.tab_image)?.setImageDrawable(resources.getDrawable(aTabs[i].image))

            if (aTabs[i].count > 0) {
                view.findViewById<TextView?>(R.id.count)?.visibility = View.VISIBLE
                view.findViewById<TextView?>(R.id.count)?.text = aTabs[i].count.toString()
            }
            tab.customView = view

            tabs.addTab(tab, 0)
        }
    }

    data class TabData (val image: Int, val count: Int)

    override fun onBackPressed() {
        when {
            mAuthHandler?.getAuthState() == AuthHandler.STATE_OFFLINE_LOGIN -> mAuthHandler?.setAuthState(AuthHandler.STATE_OFFLINE)
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_story -> {
                behaviour?.state = BottomSheetBehavior.STATE_EXPANDED

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_new_group -> {
                val i = Intent(this, NewChatActivity::class.java)
                startActivity(i)
            }
            R.id.nav_logout -> {
                logout()
            }
            R.id.nav_add_friend -> {
                val i = Intent(this, NewFriendsActivity::class.java)
                startActivity(i)
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FriendRequestFragment (defined as a static inner class below).
            return if (position == 2)CameraFragment.newInstance(position + 1)
            else PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    inner class CameraTransform : ViewPager.SimpleOnPageChangeListener(), ViewPager.PageTransformer {

        private var trans: View? = null
        private var cam: View? = null

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            trans?.apply {
                // get an instance of the bottom value of the appBar
                val h = appbar.bottom

                when (position) {
                    2 -> {
                        //translationX = pageWidth - positionOffset * pageWidth

                        val factor = 180 *  (1 - positionOffset)

                        val i = arrayOf(R.id.c, R.id.camera,
                                R.id.g, R.id.gallery,
                                R.id.v, R.id.video)

                        var c = 0

                        arrayOf(180, 300, 420).forEach { rad ->
                            cam?.findViewById<View?>(i[c])?.rotation = rad - factor
                            cam?.findViewById<View?>(i[c + 1])?.rotation = -(rad - factor)
                            c += 2
                        }

                        // Translate the appBar up as the resideReveal slides into view
                        var y = (positionOffset * h) - h
                        if (y == -h.toFloat()) y = -h - h / 8f
                        appbar.translationY = y

                        cam?.apply {
                            var r = width - (width * positionOffset)
                            val a = (1 - positionOffset) * 0.75f

                            if (positionOffset == 0f) r = 0f

                            translationX = r
                            findViewById<View?>(R.id.camera_buttons)?.translationX = -r
                            findViewById<View?>(R.id.camera_scrim)?.alpha = a
                        }

                    }
                    1 -> {

                        val factor = 180 * positionOffset

                        val i = arrayOf(R.id.c, R.id.camera,
                                R.id.g, R.id.gallery,
                                R.id.v, R.id.video)

                        var c = 0

                        arrayOf(180, 300, 420).forEach { rad ->
                            cam?.findViewById<View?>(i[c])?.rotation = rad - factor
                            cam?.findViewById<View?>(i[c + 1])?.rotation = -(rad - factor)
                            c += 2
                        }
                        appbar.translationY = -(positionOffset * h)

                        cam?.apply {
                            var r = width - (width * positionOffset)
                            val a = positionOffset * 0.75f

                            if (positionOffset == 0f) r = 0f

                            translationX = - r
                            findViewById<View?>(R.id.camera_buttons)?.apply{
                                translationX = r
                                val t = -appbar.bottom.toFloat() / 2f
                                if (translationY != t)
                                    translationY = t
                            }
                            findViewById<View?>(R.id.camera_scrim)?.alpha = a
                        }

                    }
                    else -> {
                        if (translationX != 0f) {

                        }
                        if (appbar.translationX != 0f) {
                            appbar.translationX = 0f
                        }
                    }
                }
            }
        }


        override fun transformPage(page: View, position: Float) {
            page.apply {
                when (tag as Int) {
                    1 -> if (trans != this) trans = this
                    2 -> if (cam != this) cam = this
                }

                if (trans == this && position == -1f) {

                } else if (cam == this && position == 0f) {
                    appbar.translationY = - appbar.bottom.toFloat()

                    cam?.findViewById<View?>(R.id.camera_buttons)?.apply {
                        val t = -appbar.bottom.toFloat() / 2f
                        if (translationY != t)
                            translationY = t
                    }
                }
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main2, container, false)

            when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                2 -> rootView.tag = 1
                else -> rootView.tag = 0
            }

            rootView.recent.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = RecentAdapter(context, arguments?.getInt(ARG_SECTION_NUMBER)?:-1)
            }

            return rootView

        }


        class RecentAdapter(private val context: Context, private val type: Int) : RecyclerView.Adapter<RecentHolder>() {

            override fun onBindViewHolder(holder: RecentHolder, position: Int) {
                holder.bind(type)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder {

                if (viewType == 0) return RecentHolder(LayoutInflater.from(context).inflate(R.layout.list_header, parent, false))

                return when (type) {
                    2 -> RecentHolder(LayoutInflater.from(context).inflate(R.layout.recent_messages, parent, false))
                    1 -> RecentHolder(LayoutInflater.from(context).inflate(R.layout.recent_calls, parent, false))
                    else -> RecentHolder(FrameLayout(context))
                }
            }

            override fun getItemViewType(position: Int): Int {
                return when (position) {
                    0 -> 0
                    itemCount - 1 -> 0
                    else -> 1
                }
            }

            override fun getItemCount(): Int {
                return 10 + 2
            }

        }

        class RecentHolder(val item: View) : RecyclerView.ViewHolder(item) {

            fun bind(type: Int) {
                if (type == 2) {
                    item.setOnClickListener {
                        val intent = Intent(item.context, ChatActivity::class.java)
                        item.context.startActivity(intent)
                    }
                    item.findViewById<View?>(R.id.action)?.setOnClickListener {
                        val intent = Intent(item.context, StatusActivity::class.java)
                        item.context.startActivity(intent)
                    }
                }
                else {
                    item.setOnClickListener {
                        val intent = Intent(item.context, CallInfoActivity::class.java)
                        item.context.startActivity(intent)
                    }
                    item.findViewById<View?>(R.id.action)?.setOnClickListener {
                        val intent = Intent(item.context, CallActivity::class.java)
                        item.context.startActivity(intent)
                    }
                }

                item.findViewById<View?>(R.id.dp)?.setOnClickListener {
                    ProfileDialog(item.context).show()
                }

            }
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
