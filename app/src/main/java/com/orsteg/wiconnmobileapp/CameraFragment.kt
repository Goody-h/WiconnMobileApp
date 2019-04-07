package com.orsteg.wiconnmobileapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.camera_layout.view.*

/**
 * Created by goodhope on 12/18/18.
 */
/**
 * A placeholder fragment containing a simple view.
 */
class CameraFragment : Fragment() {

    var camInterface: CameraInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context is CameraInterface) {
            camInterface = context as CameraInterface
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.camera_layout, container, false)

        rootView.tag = 2
        rootView.cam.setOnClickListener {
            camInterface?.closeGetMedia()
        }
        rootView.gallery.setOnClickListener {
            camInterface?.openGallery()
        }
        rootView.video.setOnClickListener {
            camInterface?.openVideo()
        }
        rootView.camera.setOnClickListener {
            camInterface?.openCamera()
        }
        rootView.back.setOnClickListener {  }
        return rootView

    }

    interface CameraInterface {
        fun openCamera()
        fun closeGetMedia()
        fun openVideo()
        fun openGallery()
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
        fun newInstance(sectionNumber: Int): CameraFragment {
            val fragment = CameraFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}