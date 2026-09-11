package com.call2cloud.app.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.call2cloud.app.data.db.AppDatabase
import com.call2cloud.app.data.model.UploadStatus
import com.call2cloud.app.data.preferences.SettingsManager
import com.call2cloud.app.drive.GoogleDriveManager
import kotlinx.coroutines.flow.first
import java.io.File

class UploadWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString("key_recording_id") ?: return Result.failure()
        val db = AppDatabase.getInstance(context)
        val dao = db.recordingDao()
        val settings = SettingsManager(context)
        val drive = GoogleDriveManager(context)

        val rec = dao.getRecordingById(id) ?: return Result.failure()
        val localFile = File(rec.localFilePath)
        if (!localFile.exists()) return Result.failure()

        return try {
            dao.updateUploadResult(id, UploadStatus.UPLOADING)
            val folder = settings.driveFolderName.first()
            val result = drive.uploadAndVerify(localFile, rec.filename, rec.format, rec.timestamp, folder)

            dao.updateUploadResult(id, UploadStatus.UPLOADED, result.fileId, System.currentTimeMillis(), result.md5)

            // Auto-delete if enabled
            if (settings.autoDeleteAfterUpload.first()) {
                if (localFile.delete()) {
                    dao.markLocalDeleted(id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            dao.updateUploadResult(id, UploadStatus.FAILED, error = e.message)
            if (runAttemptCount < 4) Result.retry() else Result.failure()
        }
    }
}