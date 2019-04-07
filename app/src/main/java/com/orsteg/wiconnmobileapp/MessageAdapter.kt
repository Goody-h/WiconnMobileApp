package com.orsteg.wiconnmobileapp

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by goodhope on 12/17/18.
 */
class MessageAdapter(val context: Context, val parent: RecyclerView) : RecyclerView.Adapter<MessageHolder>() {

    var lastTime: Long = 1172800000
    var init = true
    var mid = ""

    var sm: SortedMap<String, Message> = HashMap<String, Message>().toSortedMap(Comparator { id1, id2 ->
        val i = id1.indexOf("&&")
        val i2 = id2.indexOf("&&")
        val o1 = id1.substring(0, i).toLong()
        val o2 = id2.substring(0, i2).toLong()
        if (o1 > o2) 1 else if (o1 < o2) -1 else 0
    })


    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.bindMessage(sm[sm.keys.elementAt(position)]!!)
    }

    override fun getItemCount(): Int {
        return sm.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {

        val owner: Int = viewType / 1000
        val level: Int = (viewType % 1000) / 100
        val special: Int = (viewType % 100) / 10

        val type: Int = (viewType % 10)

        val view = when (owner) {
            1 -> LayoutInflater.from(context).inflate(R.layout.message_user, parent, false)
            2 -> LayoutInflater.from(context).inflate(R.layout.message_others, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.message_others, parent, false)
        }

        val back = when (owner) {
            1 -> arrayOf(R.drawable.message_user1, R.drawable.message_user2, R.drawable.message_user3, R.drawable.message_user4)
            2 -> arrayOf(R.drawable.message_other1, R.drawable.message_other2, R.drawable.message_other3, R.drawable.message_other4)
            else -> null
        }

        if (owner in 1..2) {
            when (level) {
                1 -> {
                    view.findViewById<View>(R.id.t)?.visibility = View.VISIBLE
                }
                4 -> {
                    view.findViewById<View>(R.id.b)?.visibility = View.VISIBLE
                }
                2 -> {
                    view.findViewById<View>(R.id.t)?.visibility = View.VISIBLE
                    view.findViewById<View>(R.id.b)?.visibility = View.VISIBLE
                }
            }
        }

        if (owner == 2 && (level == 2 || level == 4)) {
            view.findViewById<View>(R.id.dp)?.visibility = View.VISIBLE
        }

        if (owner == 2 && (level == 2 || level == 1)) {
            view.findViewById<View>(R.id.username)?.visibility = View.VISIBLE
        }

        when (special) {
            1 -> {
                view.findViewById<View>(R.id.date)?.visibility = View.VISIBLE
            }
            2 -> {
                view.findViewById<View>(R.id.new_message)?.visibility = View.VISIBLE
            }
            3 -> {
                view.findViewById<View>(R.id.date)?.visibility = View.VISIBLE
                view.findViewById<View>(R.id.new_message)?.visibility = View.VISIBLE
            }
        }

        if (owner in 1..2 && back != null) {
            view?.findViewById<View>(R.id.m)?.background = context.resources.getDrawable(back[level - 1])
        }

        return when (type) {
            1 -> {PlainText(view)}
            2 -> {Audio(view)}
            6 -> {Image(view)}
            else -> {PlainText(view)}
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val m = sm[sm.keys.elementAt(position)]
        val owner = if (m!!.username == "my") 1000 else if (m.type == "ADMIN") 3000 else 2000
        val m1 = if(position != 0)sm[sm.keys.elementAt(position - 1)] else null
        val m2 = if(position != itemCount - 1)sm[sm.keys.elementAt(position + 1)] else null

        var level = if (isEquivalent(m, m1) && isEquivalent(m, m2)) {
            300
        } else if (isEquivalent(m, m1)){
            400
        } else if (isEquivalent(m, m2)){
            100
        } else {
            200
        }

        val special = if (isNewDay(m, m1) && isNewMessage(m, m1)){
            30
        } else if (isNewDay(m, m1)) {
            10
        } else if (isNewMessage(m, m1)) {
            20
        } else {
            0
        }

        if (special != 0 && level/100 in 3..4) level -= 200

        val type = when(m.type) {
            "PLAIN_TEXT" -> 1
            "VOICE_NOTE" -> 2
            "AUDIO" -> 3
            "DOCUMENT" -> 4
            "VIDEO" -> 5
            "IMAGE" -> 6
            "ADMIN" -> 0
            else -> 1
        }

        return owner + level + special + type
    }

    private fun isNewDay(m: Message, m1: Message?): Boolean {
        val d = Calendar.getInstance()
        val d1 = Calendar.getInstance()
        d.timeInMillis = m.timestamp
        d.set(Calendar.MILLISECOND, 0)
        d.set(Calendar.SECOND, 0)
        d.set(Calendar.MINUTE, 0)
        d.set(Calendar.HOUR, 0)

        d1.timeInMillis = m1?.timestamp ?: 0
        d1.set(Calendar.MILLISECOND, 0)
        d1.set(Calendar.SECOND, 0)
        d1.set(Calendar.MINUTE, 0)
        d1.set(Calendar.HOUR, 0)

        return m1 == null || d.timeInMillis != d1.timeInMillis
    }

    private fun isNewMessage(m: Message, m1: Message?): Boolean {

        val new = (m.timestamp > lastTime && init && (m1 == null || m1.timestamp > m.timestamp || m1.timestamp <= lastTime)) || mid == m.getMap()
        if (new) {
            mid = m.getMap()
            init = false
        }
        return new
    }

    private fun isEquivalent(m: Message, m1: Message?): Boolean {

        return m.username == m1?.username && !isNewDay(m, m1)
    }



    fun addMessages(messages: ArrayList<Message>, start: Boolean = false) {

        var last = ""
        var pick: Long = 0

        messages.forEach { message ->
            if (!sm.contains(message.getMap())) {
                sm.put(message.getMap(), message)
                if ((pick == 0L && message.timestamp > lastTime) || (message.timestamp in (lastTime + 1)..(pick - 1))) {
                    pick = message.timestamp
                    last = message.getMap()
                }
            }
        }
        if (!init || !start){
            setLastMessage(sm[sm.keys.last()]!!.timestamp)
            last = ""
            mid = ""
        }
        notifyDataSetChanged()

        val pos = if (last != "") sm.keys.indexOf(last) else sm.size - 1

        scroll(pos)
    }



    fun addMessage(message: Message) {
        val lastm = lastTime
        val lstm = sm[sm.keys.last()]!!.timestamp

        if (!sm.contains(message.getMap())) sm.put(message.getMap(), message)
        if (!init){
            setLastMessage(sm[sm.keys.last()]!!.timestamp)
            mid = ""
        }
        if (lstm <= lastm) notifyItemInserted(sm.keys.indexOf(message.getMap()))
        else notifyDataSetChanged()
        scroll(end = true)
    }

    fun removeMessages(keys: ArrayList<String>) {
        keys.forEach { key ->
            sm.remove(key)
        }
        notifyDataSetChanged()
    }
    fun removeMessage(key: String) {
        val removed = sm.keys.indexOf(key)
        sm.remove(key)
        notifyItemRemoved(removed)
    }

    fun updateMessages(messages: ArrayList<Message>) {
        messages.forEach { message ->
            if (sm.contains(message.getMap())) sm.remove(message.getMap())
            sm.put(message.getMap(), message)
        }
        notifyDataSetChanged()
    }

    fun updateMessage(message: Message) {
        if (sm.contains(message.getMap())) sm.remove(message.getMap())
        sm.put(message.getMap(), message)
        notifyItemChanged(sm.keys.indexOf(message.getMap()))
    }

    private fun setLastMessage(time: Long) {
        lastTime = time
    }

    fun scroll(pos: Int = sm.size -1, end: Boolean = false) {
        if (end){
            parent.scrollToPosition(pos)
        } else {
            if (pos > 0) {
                parent.scrollToPosition(pos -1)
            }
        }
    }
}

open class MessageHolder(itemView: View,
                         stubId: Int,
                         val stub: View = itemView.findViewById<View>(R.id.m).findViewById<ViewStub>(stubId).inflate(),
                         private val userName: TextView? = itemView.findViewById(R.id.username),
                         val dp: ImageView? = itemView.findViewById(R.id.dp),
                         val time: TextView = itemView.findViewById(R.id.time),
                         val date: TextView? = itemView.findViewById(R.id.date),
                         val status: View? = itemView.findViewById(R.id.start))
    : RecyclerView.ViewHolder(itemView) {

    open fun bindMessage(message: Message) {
        userName?.text = message.username
        val d = Calendar.getInstance()
        d.timeInMillis = message.timestamp

        val spft = SimpleDateFormat("HH:mm")

        time.text = spft.format(d.time)

        userName?.text = message.username

        if (date?.visibility == View.VISIBLE) {
            val spf = SimpleDateFormat("dd MMM yyyy")
            val dat = spf.format(d.time)
            date.text = dat
        }
    }
}

open class PlainText(itemView: View) : MessageHolder(itemView, R.id.plain_text_stub) {

    val text: TextView = stub.findViewById(R.id.text)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)
        text.text = message.jsonContent?.optString("text", "message not available")
    }
}

class Audio(itemView: View, val length: TextView = itemView.findViewById(R.id.text),
            val size: TextView = itemView.findViewById(R.id.spread_inside),
            val pic: ImageView = itemView.findViewById(R.id.pin)) : MessageHolder(itemView, R.id.plain_text_stub){
    override fun bindMessage(message: Message) {
    }
}

class Document(itemView: View, val title: TextView = itemView.findViewById(R.id.text),
               val icon: ImageView = itemView.findViewById(R.id.icon),
               val meta: TextView = itemView.findViewById(R.id.text)) : MessageHolder(itemView, R.id.plain_text_stub){
    override fun bindMessage(message: Message) {
    }
}

class Image(itemView: View) : MessageHolder(itemView, R.id.image_stub) {

    val text: TextView = stub.findViewById(R.id.text)

    val image: ImageView = stub.findViewById(R.id.image_container)

    override fun bindMessage(message: Message) {
        super.bindMessage(message)

        val t = message.jsonContent?.optString("text", "")
        if (t != "") {
            text.visibility = View.VISIBLE
            text.text = message.jsonContent?.optString("text", "")
        } else {
            text.visibility = View.GONE
        }
    }

}
