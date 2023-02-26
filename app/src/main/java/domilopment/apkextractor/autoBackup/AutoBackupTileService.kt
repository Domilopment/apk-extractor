package domilopment.apkextractor.autoBackup

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager

class AutoBackupTileService : TileService() {
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
            stopService(Intent(this, AutoBackupService::class.java))
            qsTile.state = Tile.STATE_INACTIVE
        } else {
            updateAutoBackupPreference(true)
            startForegroundService(Intent(this, AutoBackupService::class.java))
            qsTile.state = Tile.STATE_ACTIVE
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
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean("auto_backup", value).apply()
    }
}