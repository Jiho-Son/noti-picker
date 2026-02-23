package com.example.notipicker

import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationKeywordListenerService : NotificationListenerService() {

    private val repo: KeywordRepository by lazy { KeywordRepository(this) }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val notification = sbn.notification ?: return
        val text = extractNotificationText(notification)
        if (text.isEmpty()) return

        val keywords = repo.getKeywords()
        if (keywords.isEmpty()) return

        val lowerText = text.lowercase()
        val hit = keywords.firstOrNull { keyword ->
            lowerText.contains(keyword.lowercase())
        } ?: return

        triggerVibration()
    }

    private fun extractNotificationText(notification: Notification): String {
        val extras = notification.extras
        val parts = mutableListOf<String>()

        extras.getCharSequence(Notification.EXTRA_TITLE)?.let { parts.add(it.toString()) }
        extras.getCharSequence(Notification.EXTRA_TEXT)?.let { parts.add(it.toString()) }
        extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.let { parts.add(it.toString()) }
        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.let { parts.add(it.toString()) }

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (lines != null) {
            parts.addAll(lines.map { it.toString() })
        }

        return parts.joinToString(" ")
    }

    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(
                600L,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(600L)
        }
    }
}

