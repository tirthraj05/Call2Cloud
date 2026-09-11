package com.call2cloud.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.call2cloud.app.audio.AudioRecordEngine
import com.call2cloud.app.data.db.AppDatabase
import com.call2cloud.app.data.model.CallRecording
import com.call2cloud.app.data.model.CallType
import com.call2cloud.app.data.model.UploadStatus
import com.call2cloud.app.data.preferences.SettingsManager
import com.call2cloud.app.upload.UploadWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class CallRecordingService : Service() {
    companion object {
        const val CHANNEL_ID = "call_recording_foreground_channel"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_CANCEL = "ACTION_CANCEL"

        fun startRecording(context: Context, callType: CallType) {
            val intent = Intent(context, CallRecordingService::class.java).apply {
                action = ACTION_START
                putExtra("extra_type", callType.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            context.startService(Intent(context, CallRecordingService::class.java).apply { action = ACTION_STOP })
        }

        fun cancelRecording(context: Context) {
            context.startService(Intent(context, CallRecordingService::class.java).apply { action = ACTION_CANCEL })
        }
    }

    private lateinit var audioEngine: AudioRecordEngine
    private lateinit var database: AppDatabase
    private lateinit var settings: SettingsManager
    private val scope = CoroutineScope(Dispatchers.IO)
    private var callType = CallType.INCOMING

    override fun onCreate() {
        super.onCreate()
        audioEngine = AudioRecordEngine(this)
        database = AppDatabase.getInstance(this)
        settings = SettingsManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                callType = CallType.valueOf(intent.getStringExtra("extra_type") ?: CallType.INCOMING.name)
                startForegroundNotification()
                scope.launch {
                    val fmt = settings.audioFormat.first()
                    val rate = settings.audioBitrate.first()
                    audioEngine.startRecording(callType.name, fmt, rate)
                }
            }
            ACTION_STOP -> {
                scope.launch {
                    val result = audioEngine.stopRecording()
                    if (result != null && result.durationMs > 1000) {
                        val recId = UUID.randomUUID().toString()
                        val recording = CallRecording(
                            id = recId,
                            filename = result.filename,
                            callType = callType,
                            timestamp = System.currentTimeMillis(),
                            durationMs = result.durationMs,
                            fileSizeBytes = result.fileSizeBytes,
                            localFilePath = result.file.absolutePath,
                            format = result.format,
                            bitrateKbps = result.bitrateKbps,
                            uploadStatus = UploadStatus.PENDING
                        )
                        database.recordingDao().insert(recording)
                        if (settings.autoBackup.first()) {
                            UploadWorkManager.enqueueUpload(applicationContext, recId)
                        }
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
            ACTION_CANCEL -> {
                audioEngine.cancelRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call2Cloud - Recording Active")
            .setContentText("Recording call audio via official Android API")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1001, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}