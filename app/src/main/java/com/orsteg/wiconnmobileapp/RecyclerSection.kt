package com.orsteg.wiconnmobileapp

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection

/**
 * Created by goodhope on 1/29/19.
 */
abstract class RecyclerSection(itemResId: Int, val type: Int = TYPE_PLAIN,
                               private val headerLabel: String? = null, headerResId: Int? = null,
                               footerResId: Int? = null) :
        StatelessSection(run {
            val param = when (type) {
                TYPE_PLAIN -> {
                    SectionParameters.builder().itemResourceId(itemResId)
                }
                TYPE_HEADER -> {
                    SectionParameters.builder().itemResourceId(itemResId)
                            .headerResourceId(headerResId?:R.layout.header_1)
                }
                TYPE_FOOTER -> {
                    SectionParameters.builder().itemResourceId(itemResId)
                            .footerResourceId(footerResId?:R.layout.list_header)
                }
                TYPE_HEADER_AND_FOOTER -> {
                    SectionParameters.builder().itemResourceId(itemResId)
                            .headerResourceId(headerResId?:R.layout.header_1)
                            .footerResourceId(footerResId?:R.layout.list_header)
                }
                else -> {
                    SectionParameters.builder()
                }
            }
            param.build()
        }) {

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return HeaderViewHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as? HeaderViewHolder)?.bind(headerLabel)
    }

    class HeaderViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView? = kotlin.run {
            val l = itemView?.findViewById<View?>(R.id.title)
            l as? TextView
        }
        fun bind(text: String?) {
            text?.apply {
                label?.text = this
            }
        }
    }

    companion object {
        val TYPE_PLAIN = 0
        val TYPE_HEADER = 1
        val TYPE_FOOTER = 2
        val TYPE_HEADER_AND_FOOTER = 3
    }
}