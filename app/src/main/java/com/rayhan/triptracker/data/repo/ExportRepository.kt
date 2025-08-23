package com.rayhan.triptracker.data.repo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.rayhan.triptracker.data.db.entity.TrackPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportTripToCsv(tripId: Long, points: List<TrackPoint>): Uri? =
        withContext(Dispatchers.IO) {
            val filename = "trip_$tripId.csv"

            return@withContext try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val uri = resolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    uri?.let {
                        resolver.openOutputStream(it)?.use { os ->
                            os.writer().use { writer -> writeCsv(points, writer) }
                        }
                        resolver.update(
                            it,
                            ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                            null,
                            null
                        )
                    }
                    uri
                } else {
                    // Before Android Q
                    val downloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    if (!downloads.exists()) downloads.mkdirs()

                    val file = File(downloads, filename)
                    file.printWriter().use { writeCsv(points, it) }
                    Uri.fromFile(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun writeCsv(points: List<TrackPoint>, writer: Appendable) {
        writer.appendLine("timestamp,lat,lng,speed_mps")
        for (p in points) {
            writer.appendLine("${p.timestamp},${p.lat},${p.lng},${p.speedMps}")
        }
    }
}
