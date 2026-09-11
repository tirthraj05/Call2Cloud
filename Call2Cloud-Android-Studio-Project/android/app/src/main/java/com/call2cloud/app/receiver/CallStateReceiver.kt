package com.call2cloud.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.call2cloud.app.data.model.CallType
import com.call2cloud.app.service.CallRecordingService

/**
 * State machine: IDLE -> RINGING -> OFFHOOK (connected) -> DISCONNECTED
 * "Do not record when the call was not actually connected."
 */
class CallStateReceiver : BroadcastReceiver() {
    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var isIncoming = false
        private var wasCallConnected = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val state = when (stateStr) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            else -> return
        }

        if (state == lastState) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                wasCallConnected = false
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.CALL_STATE_RINGING) isIncoming = false
                wasCallConnected = true
                val callType = if (isIncoming) CallType.INCOMING else CallType.OUTGOING
                CallRecordingService.startRecording(context, callType)
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (wasCallConnected) {
                    CallRecordingService.stopRecording(context)
                } else {
                    CallRecordingService.cancelRecording(context)
                }
                wasCallConnected = false
                isIncoming = false
            }
        }
        lastState = state
    }
}