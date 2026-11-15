package com.my.chery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.my.chery.data.VehicleData
import com.my.chery.worker.WidgetContextData
import com.my.chery.worker.cancelWidgetUpdatesIfUnused
import com.my.chery.worker.scheduleWidgetUpdates
import com.my.chery.worker.startUpdateWorker
import java.util.Locale

open class CheryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scheduleWidgetUpdates(context)
        startUpdateWorker(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) startUpdateWorker(context)
    }

    override fun onDisabled(context: Context) = cancelWidgetUpdatesIfUnused(context)

    companion object {
        const val ACTION_REFRESH = "com.my.chery.action.REFRESH"

        fun update(context: Context, manager: AppWidgetManager, id: Int, data: WidgetContextData) {
            val views = RemoteViews(context.packageName, R.layout.chery_widget)
            val vehicle = data.vehicle
            views.setTextViewText(R.id.tv_car_name, vehicle?.carName ?: "Chery Linker")
            views.setTextViewText(R.id.tv_range, rangeOf(vehicle)?.toString() ?: "--")
            views.setTextViewText(R.id.tv_update, if (data.error.isNotBlank()) data.error else "更新于 ${data.updateTime}")
            views.setTextViewText(R.id.tv_lock, lockText(vehicle))
            views.setTextViewText(R.id.tv_fuel, "油量 ${percentText(vehicle?.fuelPercent)}")
            views.setViewVisibility(R.id.tv_fuel, if (vehicle?.fuelPercent == null) View.GONE else View.VISIBLE)
            views.setTextViewText(R.id.tv_battery, "电量 ${percentText(vehicle?.batteryPercent)}")
            views.setTextViewText(R.id.tv_odometer, "里程 ${vehicle?.odometerKm?.let { format(it) + " km" } ?: "--"}")
            views.setTextViewText(R.id.tv_address, data.address.ifBlank {
                if (vehicle?.latitude != null && vehicle.longitude != null) "${vehicle.latitude}, ${vehicle.longitude}" else "位置未知"
            })
            views.setOnClickPendingIntent(R.id.widget_root, activityIntent(context))
            views.setOnClickPendingIntent(R.id.tv_refresh, refreshIntent(context, CheryWidget::class.java, id))
            manager.updateAppWidget(id, views)
        }

        internal fun activityIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        internal fun refreshIntent(context: Context, type: Class<*>, id: Int): PendingIntent {
            val intent = Intent(context, type).setAction(ACTION_REFRESH)
            return PendingIntent.getBroadcast(
                context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        internal fun rangeOf(vehicle: VehicleData?): Int? = vehicle?.rangeKm
            ?: vehicle?.let { listOfNotNull(it.fuelRangeKm, it.electricRangeKm).takeIf(List<Int>::isNotEmpty)?.sum() }

        internal fun percentText(value: Double?): String {
            if (value == null) return "--"
            val percent = when {
                value in 0.0..1.0 -> value * 100
                value > 100.0 && value <= 1000.0 -> value / 10
                else -> value
            }.coerceIn(0.0, 100.0)
            return "${percent.toInt()}%"
        }

        internal fun lockText(vehicle: VehicleData?): String = when {
            vehicle?.anyDoorOpen == true -> "车门开启"
            vehicle?.locked == true -> "已闭锁"
            vehicle?.locked == false -> "未闭锁"
            else -> "状态未知"
        }

        internal fun format(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString()
        else String.format(Locale.getDefault(), "%.1f", value)
    }
}
