package me.innerworks.capacitor.plugin

import android.content.Context

class InnerworksMetricsFactory private constructor(){
    companion object{
        @JvmStatic
        fun create(context: Context): InnerworksMetrics{
            return InnerworksMetricsManager(context)
        }
    }
}