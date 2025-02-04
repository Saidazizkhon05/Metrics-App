package me.innerworks.capacitor.plugin

import android.view.MotionEvent
import me.innerworks.capacitor.plugin.user_metrics.UserMetricsDelegate
import org.json.JSONObject

interface InnerworksMetrics: UserMetricsDelegate{
    fun sendCollectedData(data: JSONObject, onFail:(msg: String)->Unit, onSuccess:()->Unit)
    fun setBaseUrl(url: String)
    fun setOptionalData(data:String)
}