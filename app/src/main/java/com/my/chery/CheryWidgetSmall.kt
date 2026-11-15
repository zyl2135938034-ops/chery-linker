package com.my.chery

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.RemoteViews
import com.my.chery.worker.WidgetContextData
import com.my.chery.worker.cancelWidgetUpdatesIfUnused
import com.my.chery.worker.scheduleWidgetUpdates
import com.my.chery.worker.startUpdateWorker

class CheryWidgetSmall : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scheduleWidgetUpdates(context)
        startUpdateWorker(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == CheryWidget.ACTION_REFRESH) startUpdateWorker(context)
    }

    override fun onDisabled(context: Context) = cancelWidgetUpdatesIfUnused(context)

    companion object {
        fun update(context: Context, manager: AppWidgetManager, id: Int, data: WidgetContextData) {
            val views = RemoteViews(context.packageName, R.layout.chery_widget_small)
            val vehicle = data.vehicle
            views.setTextViewText(R.id.tv_car_name, vehicle?.carName ?: "Chery Linker")
            views.setTextViewText(R.id.tv_range, CheryWidget.rangeOf(vehicle)?.toString() ?: "--")
            views.setTextViewText(R.id.tv_lock, CheryWidget.lockText(vehicle))
            views.setTextViewText(R.id.tv_fuel, "油 ${CheryWidget.percentText(vehicle?.fuelPercent)}")
            val hasFuel = vehicle?.fuelPercent != null
            views.setViewVisibility(R.id.tv_fuel, if (hasFuel) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.pb_fuel, if (hasFuel) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.tv_battery, "电 ${CheryWidget.percentText(vehicle?.batteryPercent)}")
            views.setTextViewText(R.id.tv_update, if (data.error.isNotBlank()) "刷新失败" else data.updateTime)
            views.setProgressBar(R.id.pb_fuel, 100, normalizedPercent(vehicle?.fuelPercent), false)
            views.setProgressBar(R.id.pb_battery, 100, normalizedPercent(vehicle?.batteryPercent), false)
            views.setColorStateList(R.id.pb_fuel, "setProgressTintList", ColorStateList.valueOf(context.getColor(R.color.status_green)))
            views.setColorStateList(R.id.pb_battery, "setProgressTintList", ColorStateList.valueOf(context.getColor(R.color.status_blue)))
            views.setOnClickPendingIntent(R.id.widget_root, CheryWidget.activityIntent(context))
            views.setOnClickPendingIntent(R.id.tv_refresh, CheryWidget.refreshIntent(context, CheryWidgetSmall::class.java, id))
            manager.updateAppWidget(id, views)
        }

        private fun normalizedPercent(value: Double?): Int {
            value ?: return 0
            val percent = when {
                value in 0.0..1.0 -> value * 100
                value > 100.0 && value <= 1000.0 -> value / 10
                else -> value
            }
            return percent.coerceIn(0.0, 100.0).toInt()
        }
    }
}
