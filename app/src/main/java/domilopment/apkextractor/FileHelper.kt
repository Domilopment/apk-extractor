package domilopment.apkextractor

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.*

class FileHelper(private val context: Context) {
    fun copy(
        from: String,
        to: String,
        fileName: String
    ): Boolean {
        val source: InputStream
        val out: OutputStream
        val pickedDir = DocumentFile.fromTreeUri(context, Uri.parse(to + fileName))
        return try {
            val newFile = pickedDir!!.createFile("application/vnd.android.package-archive", fileName)
            out = context.contentResolver.openOutputStream(newFile!!.uri)!!
            source = FileInputStream(from)
            source.copyTo(out)
            source.close()
            out.flush()
            out.close()
            true
        } catch (fnfe1: FileNotFoundException) {
            fnfe1.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
