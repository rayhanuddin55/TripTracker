package com.rayhan.triptracker.data.repo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.rayhan.triptracker.data.db.entity.TrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(@ApplicationContext private val context: Context) {
    fun exportTripToCsv(tripId: Long, points: List<TrackPoint>): Uri? {
        val filename = "trip_$tripId.csv"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    os.writer().use { writer ->
                        writer.appendLine("timestamp,lat,lng,speed_mps")
                        points.forEach { p ->
                            writer.appendLine("${p.timestamp},${p.lat},${p.lng},${p.speedMps}")
                        }
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } else {
            // Legacy storage for < Android 10
            val downloads =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()
            val file = File(downloads, filename)
            try {
                file.printWriter().use { out ->
                    out.println("timestamp,lat,lng,speed_mps")
                    points.forEach { p -> out.println("${p.timestamp},${p.lat},${p.lng},${p.speedMps}") }
                }
                Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


}
