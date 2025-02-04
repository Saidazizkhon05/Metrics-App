package me.innerworks.capacitor.plugin.user_metrics

import android.view.MotionEvent
import me.innerworks.capacitor.plugin.user_metrics.metrics_retriever.TouchMetricsManager
import me.innerworks.capacitor.plugin.user_metrics.metrics_retriever.TypingManager
import me.innerworks.capacitor.plugin.utils.extentions.format
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

internal class UserMetricsManager: UserMetricsDelegate{

    private var touchMetricsManager: TouchMetricsManager = TouchMetricsManager()
    private var typingManager = TypingManager()

    override fun setOnTouchEvent(event: MotionEvent) {
        touchMetricsManager.handleTouchEvent(event)
    }
    override fun textOnChange(typing: String) {
        typingManager.textOnChange(typing)
    }

    fun retrieveUserMetrics(): JSONObject{
        return JSONObject()
            .put("touch_event", touchMetricsManager.getTouchEvents())
            .put("typing_records", typingManager.actions)
    }
}

interface UserMetricsDelegate{
    fun setOnTouchEvent(event: MotionEvent)
    fun textOnChange(typing: String)
}