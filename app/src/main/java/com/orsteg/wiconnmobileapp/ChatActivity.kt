package com.orsteg.wiconnmobileapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.content_chat.*
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    var adapter: MessageAdapter? = null
    val rnd: Random = Random()
    var isIncognito = false
    var emojiKeyboard: EmojIconActions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emojiKeyboard = EmojIconActions(this, window.decorView.rootView, message, emoji)
        emojiKeyboard?.setIconsIds(R.drawable.ic_keyboard_black_24dp, R.drawable.ic_insert_emoticon_black_24dp)
        emojiKeyboard?.ShowEmojIcon()

        fab.setOnClickListener {
            sendMessage()
        }
        last.setOnClickListener {
            adapter?.scroll(end = true)
        }

        val watcher = object : TextWatcher{
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

        message.addTextChangedListener(watcher)

        adapter = MessageAdapter(this, message_list)

        message_list.layoutManager = LinearLayoutManager(this)
        message_list.adapter = adapter


        addPseudoMessages()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.chat_main, menu)

        setIncognitoState(menu.findItem(R.id.action_visibility))

        return true
    }

    fun setIncognitoState(item: MenuItem) {
        item.icon = if (isIncognito) resources.getDrawable(R.drawable.ic_visibility_off_white_24dp)
        else resources.getDrawable(R.drawable.ic_visibility_white_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            R.id.action_visibility -> {
                isIncognito = !isIncognito
                setIncognitoState(item)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    fun sendMessage() {

        val mes = message.text.toString()

        // This is a test implementation, just for updating the adapter
        if(mes != ""){
            val u = arrayOf("user", "my", "user1", "my", "user3")
            val s = rnd.nextInt(4)
            val t = Calendar.getInstance().timeInMillis

            val m = Message(u[s], t, "PLAIN_TEXT", "me1s6$t", "{\"text\":\"$mes\"}")

            adapter?.addMessage(m)

            message.setText("")

        }

    }

    fun addPseudoMessages() {
        val mes = ArrayList<Message>()
        mes.add(Message("my", 172800001, "PLAIN_TEXT", "mes51a", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("hopesy2", 172800111, "PLAIN_TEXT", "me1s6", "{\"text\":\"Hello world 5 hello hello hello hello hello hello hello hello hello hello hello hello\"}"))
        mes.add(Message("my", 172805111, "PLAIN_TEXT", "mes51a", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("hopesy1", 172800121, "PLAIN_TEXT", "me2s2", "{\"text\":\"Hello world 1\"}"))
        mes.add(Message("hopesy", 172800181, "PLAIN_TEXT", "mes13", "{\"text\":\"Hello world\"}"))
        mes.add(Message("hopesy", 172800311, "PLAIN_TEXT", "mes74", "{\"text\":\"Hello world 6\"}"))
        mes.add(Message("hopesy", 172800211, "PLAIN_TEXT", "mes75", "{\"text\":\"Hello world 6\"}"))
        mes.add(Message("hopesy", 172800110, "IMAGE", "mes46", "{\"text\":\"\",\"url\":\"https://\",\"metadata\":\".\",\"thumbnail\":\"t\"}"))
        mes.add(Message("hopesy2", 192800111, "PLAIN_TEXT", "me7s3", "{\"text\":\"Hello world 2\"}"))
        mes.add(Message("hopesy1", 272800111, "PLAIN_TEXT", "mes85", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("hopesy", 172845111, "IMAGE", "mes79", "{\"text\":\"Hello world 6\",\"url\":\"https://\",\"metadata\":\".\",\"thumbnail\":\"t\"}"))
        mes.add(Message("hopesy", 572800111, "PLAIN_TEXT", "mes41", "{\"text\":\"Hello world 3\"}"))
        mes.add(Message("hopesy2", 572800151, "PLAIN_TEXT", "mes31", "{\"text\":\"Hello world 2\"}"))
        mes.add(Message("my", 1172800311, "PLAIN_TEXT", "mesb51", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("my", 1172800141, "PLAIN_TEXT", "mes5c1", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("my", 1172800911, "IMAGE", "mes51d", "{\"text\":\"Hello world 4\",\"url\":\"https://\",\"metadata\":\".\",\"thumbnail\":\"t\"}"))
        mes.add(Message("my", 1272800111, "PLAIN_TEXT", "mes51e", "{\"text\":\"Hello world 4\"}"))
        mes.add(Message("my", 1272800151, "PLAIN_TEXT", "meas51e", "{\"text\":\"Hello world 4\"}"))

        adapter?.addMessages(mes, true)
    }
}
