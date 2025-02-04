package me.innerworks.capacitor.plugin.user_metrics.metrics_retriever

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class TouchMetricsManager {

    private val touchEvents = mutableListOf<JSONObject>()
    private var touchStartTime: Long = 0
    private var startX = 0f
    private var startY = 0f
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    fun handleTouchEvent(event: MotionEvent?) {
        event?.let {
            try {
                val touchEvent = JSONObject()
                val currentTime = SystemClock.elapsedRealtime()
                val pressure = it.pressure
                val size = it.size

                when (it.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        // Capture the initial touch point
                        startX = it.x
                        startY = it.y
                        touchStartTime = currentTime
                        activePointerId = it.getPointerId(0)  // Track the first pointer
                        populateTouchEvent(touchEvent, ACTION_DOWN, it.x, it.y, currentTime, pressure, size)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val pointerIndex = it.findPointerIndex(activePointerId)
                        if (pointerIndex != -1) {
                            populateTouchEvent(
                                touchEvent,
                                ACTION_MOVE,
                                it.getX(pointerIndex),
                                it.getY(pointerIndex),
                                currentTime,
                                pressure,
                                size
                            )
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val endX = it.x
                        val endY = it.y
                        populateTouchEvent(touchEvent, ACTION_UP, endX, endY, currentTime, pressure, size)
                        touchEvent.put("duration", currentTime - touchStartTime)
                        if (isClick(startX, startY, endX, endY, touchStartTime, currentTime)) {
                            touchEvent.put("event", CLICK)
                        }
                        activePointerId = MotionEvent.INVALID_POINTER_ID  // Reset pointer tracking
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        // Handle multi-touch case, new pointer added
                        val pointerIndex = it.actionIndex
                        activePointerId = it.getPointerId(pointerIndex)
                        startX = it.getX(pointerIndex)
                        startY = it.getY(pointerIndex)
                        touchStartTime = currentTime
                        populateTouchEvent(touchEvent, ACTION_DOWN, startX, startY, currentTime, pressure, size)
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        // Handle multi-touch case, pointer lifted
                        val pointerIndex = it.actionIndex
                        val endX = it.getX(pointerIndex)
                        val endY = it.getY(pointerIndex)
                        populateTouchEvent(touchEvent, ACTION_UP, endX, endY, currentTime, pressure, size)
                        if (isClick(startX, startY, endX, endY, touchStartTime, currentTime)) {
                            touchEvent.put("event", CLICK)
                        }
                        if (it.pointerCount > 1) {
                            // Update active pointer to another one
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            activePointerId = it.getPointerId(newPointerIndex)
                            startX = it.getX(newPointerIndex)
                            startY = it.getY(newPointerIndex)
                            touchStartTime = currentTime
                        } else {
                            activePointerId = MotionEvent.INVALID_POINTER_ID  // Reset if no pointers left
                        }
                    }
                }
                touchEvents.add(touchEvent)
            } catch (e: JSONException) {
                Log.e("TouchTrackingManager", "Error logging touch event", e)
            }
        }
    }

    private fun populateTouchEvent(
        touchEvent: JSONObject,
        eventType: String,
        x: Float,
        y: Float,
        timestamp: Long,
        pressure: Float,
        size: Float
    ) {
        try {
            touchEvent.put("event", eventType)
            touchEvent.put("x", x)
            touchEvent.put("y", y)
            touchEvent.put("timestamp", timestamp)
            touchEvent.put("pressure", pressure)
            touchEvent.put("size", size)
        } catch (e: JSONException) {
            Log.e("TouchTrackingManager", "Error populating touch event", e)
        }
    }

    private val ACTION_DOWN = "ACTION_DOWN"
    private val ACTION_MOVE = "ACTION_MOVE"
    private val ACTION_UP = "ACTION_UP"
    private val CLICK = "CLICK"
    private val MAX_MOVEMENT = 10.0f
    private val MAX_DURATION = 200L

    private fun isClick(startX: Float, startY: Float, endX: Float, endY: Float, startTime: Long, endTime: Long): Boolean {
        val distance = sqrt((endX - startX).pow(2) + (endY - startY).pow(2))
        val duration = endTime - startTime
        return distance <= MAX_MOVEMENT && duration <= MAX_DURATION
    }

    // Return the list of touch events as JSONArray
    fun getTouchEvents(): JSONArray {
        return JSONArray(touchEvents)
    }

    // Clear the touch events list
    fun clearTouchEvents() {
        touchEvents.clear()
    }
}