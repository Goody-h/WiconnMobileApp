package com.orsteg.wiconnmobileapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import kotlinx.android.synthetic.main.status_fragment.view.*
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created by goodhope on 2/4/19.
 */
class StatusFragment : Fragment() {

    var statusCount :Int = 0
    var mViewCount: Int = 0
    var isGroupActive: Boolean = false
    var mSeekTime: Long = 0

    var statusList : ArrayList<Status> = ArrayList()

    var mStatusGroupHolder: FragmentStatusGroup? = null


    var image: ImageView? = null
    var text: TextView? = null
    var video: VideoView? = null
    var comment: TextView? = null
    var download: Button? = null

    private var mStatusBuilder: StatusBuilder? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.status_fragment, container, false)

        rootView.tag = arguments?.getInt(ARG_SECTION_NUMBER)

        image = rootView.image_status
        text = rootView.text_status
        video = rootView.video_status
        comment = rootView.comment
        download = rootView.download_button

        (context as? StatusActivity)?.getProgressView()?.apply {
            mStatusBuilder = StatusBuilder(this)
        }

        val builder = StatusJSONBuilder("goodhope")

        if (statusList.size == 0)
        builder.apply {
            statusList.apply {
                mStatusBuilder?.apply {
                    build(buildTextStatus("Hello world !", Color.BLUE, "font"))?.apply {
                        add(this)
                    }
                    build(buildImageStatus("thumb", "url", "Hello world !"))?.apply {
                        add(this)
                    }
                    build(buildTextStatus("Hello world !", Color.GRAY, "font"))?.apply {
                        add(this)
                    }
                    build(buildImageStatus("thumb", "url"))?.apply {
                        add(this)
                    }
                    build(buildTextStatus("Hello world !!!", Color.MAGENTA, "font"))?.apply {
                        add(this)
                    }
                }
            }
        }

        statusCount = statusList.size

        (context as? StatusActivity)?.getProgressView()?.apply {
            mStatusGroupHolder = FragmentStatusGroup(this)
        }

        rootView.next.setOnClickListener {
            mStatusGroupHolder?.nextStatus()
        }

        rootView.previous.setOnClickListener {
            mStatusGroupHolder?.previousStatus()
        }

        rootView.next.setOnLongClickListener {
            true
        }

        rootView.previous.setOnLongClickListener {
            true
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (context as? StatusActivity)?.mNavBarUtil?.subscribeToNavWidth("fragment${arguments?.getInt(ARG_SECTION_NUMBER)}") { _, height, location ->

            when (location) {
                StatusActivity.NavBarUtil.Location.NEUTRAL -> if (height < dpToPx(context, 200f)) view.download_wrapper.setPadding(0, 0, 0, height)
                StatusActivity.NavBarUtil.Location.RIGHT -> view.download_wrapper.setPadding(0, 0, 0, 0)
                StatusActivity.NavBarUtil.Location.LEFT -> view.download_wrapper.setPadding( 0, 0, 0, 0)
            }
        }
    }

    private inner class StatusBuilder(val p: GroupedStatusProgressView) {

        fun build(statusObj: JSONObject): Status? {

            val objCheck = statusObj.run {
                opt("value") is JSONObject && opt("timestamp") is Long && opt("id") is String
            }

            if (!objCheck) {
                Log.d("mytag", "build is null")
                return null
            }

            val time = statusObj.getLong("timestamp")
            val id = statusObj.getString("id")

            return statusObj.optJSONObject("value").run {
                when {
                    opt("thumb") is String && opt("url") is String && opt("playTime") is Long -> {
                        VideoStatus(p, time, id, getString("thumb"), getString("url")
                                , getLong("playTime"), opt("text") as? String)
                    }
                    opt("thumb") is String && opt("url") is String -> {
                        ImageStatus(p, time, id, getString("thumb"), getString("url")
                                , opt("text") as? String)
                    }
                    opt("text") is String -> {
                        TextStatus(p, time, getString("text"),
                                (opt("color") as? Int)?:Color.GREEN
                                , (opt("font") as? String)?:"font")
                    }
                    else -> {
                        Log.d("mytag", "build is null 2")
                        null
                    }
                }
            }
        }
    }

    class StatusJSONBuilder(val userId: String) {

        private fun getHeader(): JSONObject = JSONObject().apply {
            put("id", userId)
            put("userId", userId)
            put("timestamp", Calendar.getInstance().timeInMillis)
        }

        fun buildVideoStatus(thumb: String, url: String, playTime: Long
                             , comment: String? = null): JSONObject = getHeader().put("value", JSONObject().apply {
            put("thumb", thumb)
            put("url", url)
            put("playTime", playTime)
            if (comment != null) put("text", comment)
        })

        fun buildImageStatus(thumb: String, url: String
                             , comment: String? = null): JSONObject = getHeader().put("value", JSONObject().apply {
            put("thumb", thumb)
            put("url", url)
            if (comment != null) put("text", comment)
        })

        fun buildTextStatus(text: String, color: Int
                            , font: String): JSONObject = getHeader().put("value", JSONObject().apply {
            put("text", text)
            put("color", color)
            put("font", font)
        })
    }


    fun setText(value: String, color: Int) {
        video?.visibility = View.GONE
        image?.visibility = View.GONE
        comment?.visibility = View.GONE
        download?.visibility = View.GONE

        text?.setBackgroundColor(color)
        text?.text = value

        text?.visibility = View.VISIBLE

    }
    
    fun setTime(time: Long) {
        
    }

    fun setVideo() {

    }

    fun setImage(bitmap: Bitmap?, isready: Boolean, comment: String?) {
        text?.visibility = View.GONE
        video?.visibility = View.GONE
        //image?.setImageBitmap(bitmap)
        image?.visibility = View.VISIBLE

       setComment(comment)
    }

    fun setComment(value: String?) {
        if (value != null) {
            comment?.text = value
            comment?.visibility = View.VISIBLE

        } else {
            comment?.visibility = View.GONE
        }
    }

    fun setPersistentViewCount() {

    }

    fun setFragmentViewCount() {

    }

    inner class FragmentStatusGroup(val progress: GroupedStatusProgressView)
        : GroupedStatusProgressView.StatusGroup() {

        init {
            setStatusView()
        }

        override fun onStatusChange(statusIndex: Int, completed: Boolean) {

            if (statusIndex == -1) {
                mViewCount = 0
                previousGroup()
                return
            }

            mViewCount = statusIndex

            if (mViewCount < statusCount) {
                setStatusView()
            }
            else {
                if (!completed) mViewCount = statusIndex - 1
                nextGroup()
            }
        }

        private fun nextGroup() {
            // call to activity to set next group
            (context as? StatusActivity)?.nextStatusGroup()
        }

        private fun previousGroup() {
            // call to activity to set previous group
            (context as? StatusActivity)?.previousStatusGroup()
        }

        override fun onStatusProgress(statusIndex: Int, progressFraction: Float, totalTime: Long, elapsedTime: Long) {
            mSeekTime = elapsedTime
        }

        override fun onStateChange(statusIndex: Int, state: Int) {

        }

        override fun showGroupLayout() {
            progress.setStatusCount(statusCount)
            progress.setViewCount(mViewCount)
            val curStatus = if (mViewCount >= statusCount) statusCount - 1 else mViewCount
            statusList[curStatus].apply {
                progress.setCurrentTotalSeekTime(getTotalSeekTime())
            }
            progress.setGroupSeekTime(mSeekTime)
        }

        override fun setAsCurrentGroup() {
            if (!isGroupActive){
                if (mViewCount >= statusCount) mViewCount = statusCount - 1

                progress.setCurrentStatusGroup(this)
                isGroupActive = true
                showGroupLayout()
                setStatusView()
            }
        }

        override fun resetGroup() {
            mSeekTime = 0
            isGroupActive = false
            setStatusView()
        }

        override fun pause() {
            val curStatus = if (mViewCount >= statusCount) statusCount - 1 else mViewCount
            statusList[curStatus].apply {
                pause()
            }
        }

        override fun play() {
            if (isGroupActive){
                val curStatus = if (mViewCount >= statusCount) statusCount - 1 else mViewCount
                statusList[curStatus].apply {
                    play()
                }
            }
        }

        private fun setStatusView() {

            val curStatus = if (mViewCount >= statusCount) statusCount - 1 else mViewCount

            statusList[curStatus].apply {
                render(isGroupActive)
            }
        }

        fun nextStatus() {
            progress.getNextStatus()
        }

        fun previousStatus() {
            progress.getPreviousStatus()
        }
    }

    abstract class Status(val progress: GroupedStatusProgressView) {
        var mTotalSeekTime: Long = 5000

        fun getTotalSeekTime(): Long = mTotalSeekTime

        fun start() {
            progress.startAutoSeek()
        }

        abstract fun pause()
        abstract fun play()
        abstract fun render(canStart: Boolean)
    }

    inner class TextStatus(progress: GroupedStatusProgressView, val time: Long, val text: String, val color: Int, val font: String) : Status(progress) {
        override fun pause() {
            progress.pauseAutoSeek()
        }

        override fun play() {
            start()
        }

        override fun render(canStart: Boolean) {
            setText(text, color)
            setTime(time)
            if (canStart) {
                progress.setCurrentTotalSeekTime(getTotalSeekTime())
                play()
            }
        }
    }


    inner class ImageStatus(progress: GroupedStatusProgressView, val time: Long, val id: String, val thumb: String
                            , val url: String, val comment: String?) : Status(progress) {
        var isReady: Boolean = true

        override fun pause() {
            progress.pauseAutoSeek()
        }

        fun getImage() {            
            
        }

        override fun play() {
            if (!isReady) {
                // fetch image
                // add image load listener then start auto seek
                // load image onto view as data arrives
                // on finish isready == true
            } else {
                start()
            }
        }

        override fun render(canStart: Boolean) {

            setImage(null, false, comment)
            setTime(time)
            // render available image/thumbnail
            // render comment
            if (canStart) {
                progress.setCurrentTotalSeekTime(getTotalSeekTime())
                play()
            }
        }
    }

    inner class VideoStatus(progress: GroupedStatusProgressView, val time: Long, val id: String, val thumb: String
                            , val url: String, val playTime: Long, val comment: String?) : Status(progress) {
        var isReady: Boolean = true

        init {
            mTotalSeekTime = 5000
        }

        override fun pause() {
            // manually stop media
            video?.pause()
        }

        override fun play() {
            if (!isReady) {
                // fetch video
                // add video load listener
                // append video
                // if video is paused, startAutoSeek video
                // on finish isready == true
            }
            video?.start()
            video?.setOnCompletionListener { m ->

            }
            video?.setOnErrorListener { mp, what, extra ->
                false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                video?.setOnInfoListener { mp, what, extra ->
                    false
                }
            }

            video?.setOnPreparedListener { m ->

            }


            // startAutoSeek available video

            // when limit reaches, pauseAutoSeek video

            // on video startAutoSeek, seek progress
        }

        override fun render(canStart: Boolean) {
            setTime(time)

            text?.visibility = View.GONE
            image?.visibility = View.GONE
            video?.visibility = View.VISIBLE

            setComment(comment)

            video?.setVideoURI(Uri.fromFile(EXTERNAL_VIDEO))

            // render available image/thumbnail
            // render comment
            if (canStart) {
                progress.setCurrentTotalSeekTime(getTotalSeekTime())
                play()
            }
        }
    }

    val EXTERNAL_VIDEO: File
        get() {
            return File(Environment.getExternalStorageDirectory(), "Movies/video.mp4")
        }


    private fun dpToPx(context: Context?, valueInDp: Float) : Float {
        val metrics = context?.resources?.displayMetrics
        return if (metrics != null) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
        else 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        (context as? StatusActivity)?.mNavBarUtil?.unSubscribeFromNavWidth("fragment${arguments?.getInt(ARG_SECTION_NUMBER)}")
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
        
        fun newInstance(sectionNumber: Int): StatusFragment {
            val fragment = StatusFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}