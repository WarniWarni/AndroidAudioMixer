package com.example.androidaudiomixer.service
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.androidaudiomixer.R

    class MixerService : Service() {

        companion object {
            private const val CHANNEL_ID = "mixer_foreground_channel"
            private const val NOTIFICATION_ID = 42

            const val ACTION_START = "com.yourcompany.androidaudiomixer.action.START"
            const val ACTION_STOP  = "com.yourcompany.androidaudiomixer.action.STOP"

            /* Convenience helpers for callers */
            fun start(ctx: Context) =
                ctx.startService(Intent(ctx, MixerService::class.java).apply { action = ACTION_START })

            fun stop(ctx: Context) =
                ctx.startService(Intent(ctx, MixerService::class.java).apply { action = ACTION_STOP })
        }

        /* ---------------------------------------------------------------------- */
        /* Android lifecycle                                                      */
        /* ---------------------------------------------------------------------- */

        override fun onCreate() {
            super.onCreate()
            createNotificationChannelIfNeeded()
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent?.action) {
                ACTION_START -> promoteToForeground()
                ACTION_STOP  -> shutdown()
                else         -> { /* no-op: unknown or null action */ }
            }
            /* Let the system restart us if killed (but w/ last Intent) */
            return START_STICKY
        }

        override fun onBind(intent: Intent?): IBinder? = null

        /* ---------------------------------------------------------------------- */
        /* Private helpers                                                        */
        /* ---------------------------------------------------------------------- */

        private fun promoteToForeground() {
            val notif = buildNotification()
            startForeground(NOTIFICATION_ID, notif)
            // TODO: startCaptureAgents(), allocateMixerCore(), etc.
        }

        private fun shutdown() {
            // TODO: gracefully stop capture, mixer threads, release resources
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        private fun buildNotification(): Notification {
            /* Tapping the notification should open the UI */
            val openAppIntent = Intent(this, Class.forName("com.yourcompany.androidaudiomixer.ui.MainActivity"))
                .let { pendingIntent(it) }

            /* Optional “Stop” action button */
            val stopIntent = Intent(this, MixerService::class.java).apply { action = ACTION_STOP }
                .let { pendingIntent(it) }

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_mixer)   // create a 24×24 white icon
                .setContentTitle("AndroidAudioMixer")
                .setContentText("Running • Tap to configure")
                .setContentIntent(openAppIntent)
                .addAction(
                    R.drawable.ic_baseline_stop_24,
                    "Stop",
                    stopIntent
                )
                .setOngoing(true)                          // can’t be swiped away
                .build()
        }

        private fun pendingIntent(intent: Intent): PendingIntent =
            PendingIntent.getActivity(
                this, intent.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        private fun createNotificationChannelIfNeeded() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                    mgr.createNotificationChannel(
                        NotificationChannel(
                            CHANNEL_ID,
                            "Audio Mixer",
                            NotificationManager.IMPORTANCE_LOW   // LOW keeps it silent
                        ).apply {
                            description = "Foreground service for AndroidAudioMixer"
                        }
                    )
                }
            }
        }
    }