package chevstrap.extensions

import android.util.AtomicFile
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets

object FileTool {
    @JvmStatic
    fun isExist(filePath: String): Boolean {
        return File(filePath).exists()
    }

    fun deleteDir(dir: File?, deleteDirectoriesToo: Boolean): Boolean {
        if (dir == null) return false
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (child in children) {
                    val success = deleteDir(File(dir, child), deleteDirectoriesToo)
                    if (!success) return false
                }
            }
            return !deleteDirectoriesToo || dir.delete()
        } else if (dir.isFile) {
            return dir.delete()
        } else {
            return false
        }
    }

    @JvmStatic
    fun deleteFile(file: File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                deleteDir(file, true)
            }
        }
    }

    fun ensureDirectoryExists(dir: File?) {
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw RuntimeException("Failed to create directory: " + dir.absolutePath)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun safeWrite(file: File, content: String) {
        val parent = file.parentFile
        ensureDirectoryExists(parent)

        val atomicFile = AtomicFile(file)
        var fos: FileOutputStream? = null

        try {
            fos = atomicFile.startWrite()
            fos.write(content.toByteArray(StandardCharsets.UTF_8))
            atomicFile.finishWrite(fos)
            fos = null
        } catch (e: IOException) {
            if (fos != null) {
                atomicFile.failWrite(fos)
            }
            throw e
        }
    }

    @Throws(IOException::class)
    fun copy(sourceFile: File, destFile: File, overwrite: Boolean) {
        if (!sourceFile.exists()) {
            throw IOException("Source file does not exist: " + sourceFile.absolutePath)
        }

        if (destFile.exists()) {
            if (overwrite) {
                if (!destFile.delete()) {
                    throw IOException("Failed to overwrite existing file: " + destFile.absolutePath)
                }
            } else {
                throw IOException("Destination file already exists: " + destFile.absolutePath)
            }
        } else {
            val parentDir: File = checkNotNull(destFile.parentFile)
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw IOException("Failed to create parent directories for: " + destFile.absolutePath)
            }
        }

        FileInputStream(sourceFile).use { `in` ->
            FileOutputStream(destFile).use { out ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while ((`in`.read(buffer).also { bytesRead = it }) != -1) {
                    out.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun listFiles(dir: File): Array<File> {
        if (!dir.exists()) {
            throw IOException("Directory does not exist: ${dir.absolutePath}")
        }
        if (!dir.isDirectory) {
            throw IOException("Not a directory: ${dir.absolutePath}")
        }

        return dir.listFiles()
            ?: throw IOException("Unable to access files in directory: ${dir.absolutePath}")
    }

    @JvmStatic
    @Throws(IOException::class)
    fun safeRead(file: File): String {
        val atomicFile = AtomicFile(file)

        atomicFile.openRead().use { input ->
            val buffer = ByteArray(8192)
            val output = ByteArrayOutputStream()

            var length: Int

            while (input.read(buffer).also { length = it } != -1) {
                output.write(buffer, 0, length)
            }

            return output.toString(StandardCharsets.UTF_8.name())
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(file: File?): String {
        val sb = StringBuilder()
        FileReader(file).use { fr ->
            val buffer = CharArray(1024)
            var length: Int
            while ((fr.read(buffer).also { length = it }) != -1) {
                sb.appendRange(buffer, 0, length)
            }
        }
        return sb.toString()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun write(file: File?, data: String?) {
        BufferedWriter(FileWriter(file)).use { writer ->
            writer.write(data)
        }
    }
}