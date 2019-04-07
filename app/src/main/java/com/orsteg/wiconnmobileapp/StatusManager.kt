package com.orsteg.wiconnmobileapp

import android.content.Context
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 * Created by goodhope on 1/29/19.
 */
class StatusManager(val context: Context, val parent: RecyclerView) {

    private var sectionAdapter: SectionedRecyclerViewAdapter? = null
    var mLayoutStyle: Int = 0

    init {
        if (mLayoutStyle == 0) useListLayout()
        else useGridLayout()
    }

    private fun initAdapter() {
        sectionAdapter = SectionedRecyclerViewAdapter()
        sectionAdapter?.addSection(StatusSection(context))
        sectionAdapter?.addSection(StatusSection(context, "Recent updates"))
        sectionAdapter?.addSection(StatusSection(context, "Viewed updates"))
    }

    fun useGridLayout() {
        mLayoutStyle = 1

        initAdapter()

        val manager = GridLayoutManager(context, 4)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (sectionAdapter?.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER ||
                        (sectionAdapter?.getSectionForPosition(position) as? StatusSection)?.label == null) {
                    return 4
                }

                return 1
            }
        }

        parent.apply {
            layoutManager = manager
            adapter = sectionAdapter
        }

    }

    fun useListLayout() {
        mLayoutStyle = 0

        initAdapter()

        val manager = LinearLayoutManager(context)

        parent.apply {
            layoutManager = manager
            adapter = sectionAdapter
        }
    }

    inner class StatusSection(val context: Context, val label: String? = null) : RecyclerSection(
            kotlin.run { if (label != null) when(mLayoutStyle) { 0 -> R.layout.story_list else -> R.layout.story_grid} else R.layout.my_story_grid },
            kotlin.run { if (label != null) TYPE_HEADER else TYPE_PLAIN }, label) {

        override fun getContentItemsTotal(): Int {
            return if (label == null) 1 else 10
        }

        override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as? StoryViewHolder)?.bind(label)
        }

        override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
            return if (view != null)
                StoryViewHolder(view)
            else
                StoryViewHolder(FrameLayout(context))
        }
    }

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(label: String?) {
            if (label!= null) {
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, StatusActivity::class.java)
                    itemView.context.startActivity(intent)
                }
                if (label == "Viewed updates") {
                    itemView.findViewById<StatusImageView?>(R.id.image)
                            ?.apply { viewCount = statusCount }
                }
            } else {
                itemView.setOnClickListener {
                    (itemView.context as? MainActivity)?.getMedia() }
            }
        }
    }

    class StoryInfo() {}

}