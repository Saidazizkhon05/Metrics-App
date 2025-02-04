package me.innerworks.capacitor.plugin.user_metrics.metrics_retriever

import me.innerworks.capacitor.plugin.utils.extentions.format
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

class TypingManager {

    val actions = JSONObject()
    private var typed = ""

    // Method to record text typing
    fun textOnChange(typing: String) {
        val timestamp = Date().format()
        try {
            if (typing.length > typed.length) { // Added
                val act = "added"
                val lastChar = typing[typing.length - 1]

                if (Character.isUpperCase(lastChar)) { // Uppercase added
                    actions.put(timestamp, "$act(uppercased)")
                } else if (!Character.isLetterOrDigit(lastChar) && !Character.isWhitespace(lastChar)) { // Symbols
                    actions.put(timestamp, "$act(symbols)")
                } else if (Character.isDigit(lastChar)) { // Number
                    actions.put(timestamp, "$act(number)")
                } else {
                    actions.put(timestamp, act)
                }
            } else { // Deleted
                actions.put(timestamp, "deleted")
            }
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }
        typed = typing
    }
}