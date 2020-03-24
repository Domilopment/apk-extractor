package domilopment.apkextractor

import java.io.File

class FileHelper(from: String, to: String, fileName: String) {
    private val from = File(from)
    private val to = File(to + fileName)
    private val path = to

    fun copy(): Boolean{
        try {
            File(path).mkdir()
            if (from.exists()) {
                from.copyTo(to)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }

    private fun File.copyTo(file: File) {
        inputStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}