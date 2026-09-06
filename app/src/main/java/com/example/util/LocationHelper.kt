package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {
    // Default fallback coordinates (e.g. vibrant street food district center)
    const val DEFAULT_LATITUDE = 24.8615
    const val DEFAULT_LONGITUDE = 67.0099

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    // Fallback to LocationManager if Google Play Services fails
                    continuation.resume(getFallbackLocation(context))
                }
            }
        } catch (e: Exception) {
            getFallbackLocation(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFallbackLocation(context: Context): Location? {
        return try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            null
        }
    }
}
