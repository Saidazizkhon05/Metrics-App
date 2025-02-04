package me.innerworks.capacitor.plugin

import android.annotation.SuppressLint
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

@CapacitorPlugin(name = "Metrics")
class MetricsPlugin: Plugin(){

    private val metricsManager: InnerworksMetricsManager by lazy {
        InnerworksMetricsManager(context)
    }

    override fun load() {
        super.load()
        setUpMetrics()
    }

    @PluginMethod
    fun setOptionalData(call: PluginCall){
        try {
            val optionalData = call.getString("data") ?: ""
            metricsManager.setOptionalData(optionalData)
            call.resolve()
        }catch (e: Exception){
            call.reject(e.localizedMessage)
        }
    }

    @SuppressLint("DefaultLocale")
    @PluginMethod
    fun textOnChange(call: PluginCall) {
        try {
            val typing = call.getString("typing")
            if (typing != null) metricsManager.textOnChange(typing)
            call.resolve()
        } catch (e: Exception) {
            call.reject(e.localizedMessage)
        }
    }

    @SuppressLint("DefaultLocale")
    @PluginMethod
    fun sendCollectedData(call: PluginCall) {
        try {
            val socialId = call.getString("socialId", "") ?: ""
            val projectId = call.getString("projectId", "") ?: ""
            if (projectId.isEmpty()) {
                call.reject("ProjectId is not set")
                return
            }
            if (socialId.isEmpty()){
                call.reject("SocialId is not set")
                return
            }
            metricsManager.sendCollectedData(
                JSONObject().put("user_id", socialId).put("projectId", projectId),
                onFail = { msg ->
                    call.reject(msg)
                },
                onSuccess = {
                    call.resolve()
                }
            )
        } catch (e: Exception) {
            call.reject(e.localizedMessage)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpMetrics(){
        bridge.webView.setOnTouchListener { _, motionEvent ->
            metricsManager.setOnTouchEvent(motionEvent)
            false
        }
        readUrlFromRawFile()?.let { baseUrl->
            metricsManager.setBaseUrl(baseUrl)
        }

    }

    private fun readUrlFromRawFile(): String? {
        val res = context.resources
        val inputStream = res.openRawResource(R.raw.config)
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            var i = inputStream.read()
            while (i != -1) {
                byteArrayOutputStream.write(i)
                i = inputStream.read()
            }
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return try {
            JSONObject(byteArrayOutputStream.toString())["api_url"].toString()
        } catch (ex: java.lang.Exception) {
            null
        }
    }
}