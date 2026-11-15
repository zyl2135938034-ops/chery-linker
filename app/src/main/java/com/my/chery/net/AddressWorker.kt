package com.my.chery.net

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object AddressWorker {
    suspend fun getAddress(context: Context, latitude: Double, longitude: Double): String {
        if (!Geocoder.isPresent()) return ""
        return try {
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { continuation ->
                    Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1) { addresses ->
                        continuation.resume(addresses.firstOrNull()?.getAddressLine(0).orEmpty())
                    }
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()?.getAddressLine(0).orEmpty()
                }
            }
        } catch (_: Exception) {
            ""
        }
    }
}
