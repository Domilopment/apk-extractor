package domilopment.apkextractor.autoBackup

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.utils.Utils
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AutoBackupTileService : TileService() {
    @Inject
    lateinit var settings: PreferenceRepository

    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
        qsTile.state = if (AutoBackupService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
        qsTile.state = if (AutoBackupService.isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        if (!areNotificationsEnabled(this)) {
            // Notifications not enabled, open settings to allow the user to enable notifications
            Toast.makeText(
                applicationContext,
                R.string.auto_backup_tile_service_notifications_disabled,
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                startActivityAndCollapse(pendingIntent)
            } else {
                startActivityAndCollapse(intent)
            }

            return
        }

        if (AutoBackupService.isRunning) {
            updateAutoBackupPreference(false)
            Intent(this, AutoBackupService::class.java).apply {
                action = AutoBackupService.Actions.STOP.name
            }.also {
                startService(it)
            }
            qsTile.state = Tile.STATE_INACTIVE
        } else {
            updateAutoBackupPreference(true)

            Intent(this, AutoBackupService::class.java).apply {
                action = AutoBackupService.Actions.START.name
            }.also {
                if (Utils.startForegroundService(applicationContext, it)) qsTile.state =
                    Tile.STATE_ACTIVE
                else Toast.makeText(
                    applicationContext,
                    R.string.auto_backup_tile_service_start_foreground_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        qsTile.updateTile()
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    /**
     * Set new value for Auto Backup shared preference with key auto_backup
     * @param value boolean value for preference, to be applied
     */
    private fun updateAutoBackupPreference(value: Boolean) {
        runBlocking {
            settings.setAutoBackupService(value)
        }
    }

    /**
     * Check if notifications are enabled for this app
     * @param context Context for access to notification manager
     * @return True if notifications are enabled
     */
    private fun areNotificationsEnabled(context: Context): Boolean {
        // Check if notifications are enabled using NotificationManager
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return manager.areNotificationsEnabled()
    }
}