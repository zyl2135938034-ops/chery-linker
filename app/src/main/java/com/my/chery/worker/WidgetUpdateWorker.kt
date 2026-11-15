package com.my.chery.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.my.chery.CheryWidget
import com.my.chery.CheryWidgetSmall
import com.my.chery.data.AppConfig
import com.my.chery.data.VehicleData
import com.my.chery.net.AddressWorker
import com.my.chery.net.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class WidgetContextData(
    val vehicle: VehicleData?,
    val address: String = "",
    val updateTime: String = "",
    val error: String = ""
)

private const val PERIODIC_WORK = "chery_widget_periodic"

fun startUpdateWorker(context: Context) {
    WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())
}

fun scheduleWidgetUpdates(context: Context) {
    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

fun cancelWidgetUpdatesIfUnused(context: Context) {
    val manager = AppWidgetManager.getInstance(context)
    val hasWidgets = listOf(CheryWidget::class.java, CheryWidgetSmall::class.java).any {
        manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty()
    }
    if (!hasWidgets) WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
}

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        val manager = AppWidgetManager.getInstance(context)
        val widgets = listOf(CheryWidget::class.java, CheryWidgetSmall::class.java)
        val active = widgets.mapNotNull { type ->
            manager.getAppWidgetIds(ComponentName(context, type)).takeIf { it.isNotEmpty() }?.let { type to it }
        }
        if (active.isEmpty()) return@withContext Result.success()

        val config = AppConfig(context)
        var vehicle: VehicleData? = null
        var address = ""
        var error = ""
        if (config.isConfigured) {
            try {
                val fetched = VehicleRepository.fetch(config)
                vehicle = fetched
                config.lastDataJson = fetched.rawJson
                val lat = fetched.latitude
                val lng = fetched.longitude
                if (lat != null && lng != null) {
                    address = AddressWorker.getAddress(context, lat, lng)
                }
            } catch (e: Exception) {
                Log.e("WidgetUpdateWorker", "车辆数据刷新失败", e)
                error = e.message ?: "网络请求失败"
            }
        } else {
            error = "请打开 App 填写凭据"
        }

        val data = WidgetContextData(
            vehicle = vehicle,
            address = address,
            updateTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            error = error
        )
        active.forEach { (type, ids) ->
            ids.forEach { id ->
                runCatching {
                    if (type == CheryWidget::class.java) CheryWidget.update(context, manager, id, data)
                    else CheryWidgetSmall.update(context, manager, id, data)
                }
            }
        }
        Result.success()
    }
}
