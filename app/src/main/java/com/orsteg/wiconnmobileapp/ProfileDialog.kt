package com.orsteg.wiconnmobileapp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import kotlinx.android.synthetic.main.profile_dialog_layout.*

/**
 * Created by goodhope on 3/29/19.
 */
class ProfileDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.profile_dialog_layout)

    }


}