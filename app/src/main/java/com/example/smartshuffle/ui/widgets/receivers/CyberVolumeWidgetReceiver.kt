package com.example.smartshuffle.ui.widgets.receivers

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.example.smartshuffle.ui.widgets.CyberVolumeWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CyberVolumeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CyberVolumeWidget()
    
    private val coroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            coroutineScope.launch {
                glanceAppWidget.updateAll(context)
            }
        }
    }
}
