package com.danimodder.dumper.utils

import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class StorageManager {

    companion object {
        private const val TAG = "StorageManager"
        private const val APP_DIR = "DaniModder"
    }

    init {
        createAppDirectory()
    }

    /**
     * Create app directory if it doesn't exist
     */
    private fun createAppDirectory() {
        try {
            val dir = File(Environment.getExternalStorageDirectory(), APP_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Log.d(TAG, "App directory: ${dir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating app directory", e)
        }
    }

    /**
     * Get dump directory path
     */
    fun getDumpDir(): File {
        return File(Environment.getExternalStorageDirectory(), APP_DIR)
    }

    /**
     * Save dump log to file
     */
    fun saveDumpLog(content: String): Boolean {
        return try {
            val logFile = File(getDumpDir(), "log.txt")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            val fullContent = """
                ========================================
                Timestamp: $timestamp
                ========================================
                $content
                
            """.trimIndent()

            logFile.appendText(fullContent)
            Log.d(TAG, "Log saved to: ${logFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving log", e)
            false
        }
    }

    /**
     * Get all dump files
     */
    fun getDumpFiles(): List<File> {
        return try {
            val dir = getDumpDir()
            dir.listFiles()?.filter { it.extension == "bin" } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting dump files", e)
            emptyList()
        }
    }

    /**
     * Get latest dump file
     */
    fun getLatestDumpFile(): File? {
        return try {
            getDumpFiles().maxByOrNull { it.lastModified() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest dump file", e)
            null
        }
    }

    /**
     * Delete dump file
     */
    fun deleteDumpFile(fileName: String): Boolean {
        return try {
            val file = File(getDumpDir(), fileName)
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting dump file", e)
            false
        }
    }

    /**
     * Get directory size
     */
    fun getDumpDirSize(): Long {
        return try {
            var size = 0L
            val dir = getDumpDir()
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
            size
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating directory size", e)
            0L
        }
    }

    /**
     * Get directory size recursively
     */
    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    /**
     * Format bytes to human readable format
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}