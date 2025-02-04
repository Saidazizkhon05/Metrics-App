package me.innerworks.capacitor.plugin

import android.content.Context
import android.view.MotionEvent
import me.innerworks.capacitor.plugin.apps.AppsMetricsManager
import me.innerworks.capacitor.plugin.battery.BatteryInfoManager
import me.innerworks.capacitor.plugin.bluetooth.BluetoothMetricsManager
import me.innerworks.capacitor.plugin.camera.CameraMetricsManager
import me.innerworks.capacitor.plugin.cpu.CPUMetricsManager
import me.innerworks.capacitor.plugin.device.DeviceMetricsManager
import me.innerworks.capacitor.plugin.display.DisplayMetricsManager
import me.innerworks.capacitor.plugin.location.LocationMetricsManager
import me.innerworks.capacitor.plugin.network.NetworkMetricsManager
import me.innerworks.capacitor.plugin.send_collected_data.SendCollectedMetrics
import me.innerworks.capacitor.plugin.sensors.SensorMetricsManager
import me.innerworks.capacitor.plugin.storage.StorageMetricsManager
import me.innerworks.capacitor.plugin.system.SystemMetricsManager
import me.innerworks.capacitor.plugin.user_metrics.UserMetricsManager
import me.innerworks.capacitor.plugin.utils.extentions.format
import org.json.JSONObject
import java.util.Date


internal class InnerworksMetricsManager(val context: Context): InnerworksMetrics{

    private val userMetricsManager = UserMetricsManager()
    private val sendCollectedMetrics = SendCollectedMetrics(context)
    private val locationMetricsManager = LocationMetricsManager(context)
    private var startSession: Date = Date()
    private val allMetrics = JSONObject()

    init {
        locationMetricsManager.startLocationUpdates()
    }

    // Public method to collect all metrics from different managers into one payload
    private fun collectAllMetrics(): JSONObject {
        try {
            // Device Metrics
            val deviceMetricsManager = DeviceMetricsManager(context)
            allMetrics.put("deviceMetrics", deviceMetricsManager.getDeviceMetrics())

            //CPU metrics
            val cpuMetricsManager = CPUMetricsManager(context)
            allMetrics.put("cpuMetrics", cpuMetricsManager.getCPUMetrics())

            //System metrics
            val systemMetricsManager = SystemMetricsManager(context)
            allMetrics.put("systemMetrics", systemMetricsManager.getSystemMetrics())

            // Network Metrics
            val networkMetricsManager = NetworkMetricsManager(context)
            allMetrics.put("networkMetrics", networkMetricsManager.getNetworkMetrics())

            // Battery Metrics
            val batteryInfoManager = BatteryInfoManager(context)
            allMetrics.put("batteryMetrics", batteryInfoManager.getBatteryInfo())

            // Storage Metrics
            val storageMetricsManager = StorageMetricsManager(context)
            allMetrics.put("storageMetrics", storageMetricsManager.getStorageMetrics())

            // Display Metrics
            val displayMetricsManager = DisplayMetricsManager(context)
            allMetrics.put("displayMetrics", displayMetricsManager.getDisplayMetrics())

            // Camera Metrics
            val cameraMetricsManager = CameraMetricsManager(context)
            allMetrics.put("cameraMetrics", cameraMetricsManager.getCameraMetrics())

            // Sensor Metrics
            val sensorMetricsManager = SensorMetricsManager(context)
            allMetrics.put("sensorMetrics", sensorMetricsManager.getFormattedSensorMetrics())

            // App Info Metrics
            val appInfoManager = AppsMetricsManager(context)
            allMetrics.put("appMetrics", appInfoManager.getCategorizedApps())

            allMetrics.put("locationMetrics", locationMetricsManager.locationArr)

            // User Metrics
            allMetrics.put("userMetrics", userMetricsManager.retrieveUserMetrics())

            //Session Metrics
            allMetrics.put("session_time", "${Date().format()}~${startSession.format()}")

            //Bluetooth Metrics
            val bluetoothMetricsManager = BluetoothMetricsManager(context)
            allMetrics.put("bluetoothMetrics", bluetoothMetricsManager.getBluetoothMetrics())

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return allMetrics
    }

    override fun sendCollectedData(data: JSONObject, onFail: (msg: String)->Unit, onSuccess: ()->Unit) {
        locationMetricsManager.stopLocationUpdates()
        val payload = collectAllMetrics()
        sendCollectedMetrics.sendPostRequest(
            data.put("metrics", payload).put("sdk_type", "Android"),
            onFail,
            onSuccess
        )
    }

    override fun setBaseUrl(url: String) {
        sendCollectedMetrics.setBaseUrl(url)
    }

    override fun setOnTouchEvent(event: MotionEvent) {
        userMetricsManager.setOnTouchEvent(event)
    }

    override fun textOnChange(typing: String) {
        userMetricsManager.textOnChange(typing)
    }

    override fun setOptionalData(data: String) {
        allMetrics.put("optionalMetrics", data)
    }
}