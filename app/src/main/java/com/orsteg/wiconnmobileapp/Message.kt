package com.orsteg.wiconnmobileapp

import org.json.JSONObject

/**
 * Created by goodhope on 12/6/18.
 */
class Message() {
    var timestamp: Long = 0
    var username: String = ""
    var type: String = ""
    var messageId: String = ""
    var status: Int = 0
    var content: String = ""
    var jsonContent: JSONObject? = null

    constructor(username: String, timestamp: Long, type: String, messageId: String, content: String ) : this() {
        this.timestamp = timestamp
        this.username = username
        this.type = type
        this.messageId = messageId
        this.content = content

        jsonContent = try {
            JSONObject(content)
        } catch (e: Exception) {
            null
        }

        parse()
    }

    fun getMap(): String {
        return "$timestamp&&$messageId"
    }

    private fun parse() {
        if(jsonContent !== null) {
            if (checkValidity(jsonContent!!)) {
                if (jsonContent!!.has("reply")) {
                    if (!checkValidity(jsonContent!!.getJSONObject("reply"))) {
                        jsonContent = null
                    }
                }
            } else {
                jsonContent = null

            }
        }
    }

    private fun checkValidity(obj: JSONObject): Boolean {
        with(obj) {
            return (type == "PLAIN_TEXT" && this.has("text")) ||
                    (type == "VOICE_NOTE" && this.has("url") && this.has("metadata")) ||
                    (type == "AUDIO" && this.has("url") && this.has("metadata")) ||
                    (type == "DOCUMENT" && this.has("url") && this.has("metadata")) ||
                    (type == "VIDEO" && this.has("url") && this.has("thumbnail") && this.has("metadata")) ||
                    (type == "IMAGE" && this.has("url") && this.has("thumbnail") && this.has("metadata"))
        }
    }
}