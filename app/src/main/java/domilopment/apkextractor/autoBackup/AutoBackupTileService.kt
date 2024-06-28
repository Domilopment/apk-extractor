package domilopment.apkextractor.autoBackup

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        startForegroundService(it)
                        qsTile.state = Tile.STATE_ACTIVE
                    } catch (e: ForegroundServiceStartNotAllowedException) {
                        Toast.makeText(
                            applicationContext,
                            R.string.auto_backup_tile_service_start_foreground_error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    startForegroundService(it)
                    qsTile.state = Tile.STATE_ACTIVE
                }
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
}