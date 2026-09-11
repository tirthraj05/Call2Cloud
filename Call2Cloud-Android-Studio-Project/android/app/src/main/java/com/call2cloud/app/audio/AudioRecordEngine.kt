package com.call2cloud.app.audio

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordEngine(private val context: Context) {
    companion object {
        fun isOpusSupported(): Boolean {
            val list = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (info in list.codecInfos) {
                if (info.isEncoder && info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_AUDIO_OPUS, true) }) {
                    return true
                }
            }
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }

    data class RecordingResult(
        val file: File,
        val filename: String,
        val format: String,
        val bitrateKbps: Int,
        val durationMs: Long,
        val fileSizeBytes: Long
    )

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0L
    private var filename: String = ""
    private var format: String = "OPUS"
    private var bitrate: Int = 48

    fun startRecording(prefix: String, targetFormat: String, targetBitrate: Int): File? {
        val useOpus = if (targetFormat == "AUTO") isOpusSupported() else targetFormat == "OPUS"
        format = if (useOpus) "OPUS" else "AAC"
        bitrate = if (useOpus) targetBitrate.coerceIn(32, 48) else targetBitrate.coerceIn(64, 96)

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val ext = if (useOpus) "opus" else "m4a"
        filename = "${prefix}_${timestamp}.$ext"

        val dir = File(context.filesDir, "Recordings").apply { mkdirs() }
        val file = File(dir, filename)
        currentFile = file

        return try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                if (useOpus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(48000)
                } else {
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                }
                setAudioChannels(1) // Mono for voice clarity & compact size
                setAudioEncodingBitRate(bitrate * 1000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            startTimeMs = System.currentTimeMillis()
            file
        } catch (e: Exception) {
            file.delete()
            currentFile = null
            null
        }
    }

    fun stopRecording(): RecordingResult? {
        val file = currentFile ?: return null
        val duration = System.currentTimeMillis() - startTimeMs
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {}
        recorder = null
        return if (file.exists() && file.length() > 0) {
            RecordingResult(file, filename, format, bitrate, duration, file.length())
        } else null
    }

    fun cancelRecording() {
        try { recorder?.stop(); recorder?.release() } catch (e: Exception) {}
        currentFile?.delete()
        recorder = null
    }
}