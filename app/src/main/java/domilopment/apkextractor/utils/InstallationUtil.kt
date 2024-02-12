package domilopment.apkextractor.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import domilopment.apkextractor.MainActivity
import java.io.InputStream

object InstallationUtil {
    fun createSession(context: Context): Pair<Session, Int> {
        val packageInstaller = context.applicationContext.packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = packageInstaller.createSession(params)

        return Pair(packageInstaller.openSession(sessionId), sessionId)
    }

    fun addFileToSession(
        session: Session, inputStream: InputStream, fileName: String, filesSize: Long
    ) {
        session.openWrite(fileName, 0, filesSize).use { outputStream ->
            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
        }
    }

    fun <T : Activity> finishSession(
        context: Context, session: Session, sessionId: Int, cls: Class<T>
    ) {
        val pendingIntent = Intent(context, cls).apply {
            action = MainActivity.PACKAGE_INSTALLATION_ACTION
        }.let {
            PendingIntent.getActivity(
                context, sessionId, it, PendingIntent.FLAG_MUTABLE
            )
        }

        session.commit(pendingIntent.intentSender)
        session.close()
    }
}