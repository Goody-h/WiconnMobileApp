package com.orsteg.wiconnmobileapp

import android.content.Context
import android.content.Intent
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import kotlinx.android.synthetic.main.activity_new_friends.*
import kotlinx.android.synthetic.main.add_friend_fragment.view.*
import kotlinx.android.synthetic.main.friend_request_fragment.view.*

class NewFriendsActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_friends)

        setSupportActionBar(toolbar)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new_friends, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FriendRequestFragment (defined as a static inner class below).
            return  when (position) {
                0 -> AddFriendFragment.newInstance(position + 1)
                else -> FriendRequestFragment.newInstance(position + 1)
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 2
        }
    }

    class FriendRequestFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.friend_request_fragment, container, false)

            rootView.requests.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = NewFriendAdapter(context, arguments?.getInt(ARG_SECTION_NUMBER)?:0)
            }


            return rootView
        }

        companion object {
            private val ARG_SECTION_NUMBER = "section_number"

            fun newInstance(sectionNumber: Int): FriendRequestFragment {
                val fragment = FriendRequestFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }

    class AddFriendFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.add_friend_fragment, container, false)
            rootView.addings.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = NewFriendAdapter(context, arguments?.getInt(ARG_SECTION_NUMBER)?:0)
            }

            return rootView
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
            fun newInstance(sectionNumber: Int): AddFriendFragment {
                val fragment = AddFriendFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }


    class NewFriendAdapter(private val context: Context, private val type: Int) : RecyclerView.Adapter<NewFriendHolder>() {

        override fun onBindViewHolder(holder: NewFriendHolder, position: Int) {
            holder.bind(type)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewFriendHolder {

            return when (type) {
                2 -> NewFriendHolder(LayoutInflater.from(context).inflate(R.layout.friend_request_item, parent, false))
                1 -> {
                    when (viewType) {
                        0 -> NewFriendHolder(LayoutInflater.from(context).inflate(R.layout.add_friend_contact, parent, false))
                        else -> NewFriendHolder(LayoutInflater.from(context).inflate(R.layout.add_friend_item, parent, false))
                    }
                }
                else -> NewFriendHolder(FrameLayout(context))
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (type == 2) return 0

            return when (position) {
                0 -> 0
                else -> 1
            }
        }

        override fun getItemCount(): Int {
            return 10 + 2
        }

    }

    class NewFriendHolder(val item: View) : RecyclerView.ViewHolder(item) {

        fun bind(type: Int) {

        }
    }

}
