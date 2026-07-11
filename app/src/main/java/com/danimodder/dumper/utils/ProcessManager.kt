package com.danimodder.dumper.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import java.io.File

class ProcessManager {

    companion object {
        private const val TAG = "ProcessManager"
    }

    /**
     * Get list of running processes
     */
    fun getRunningProcesses(context: Context): List<Pair<String, Int>> {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processes = activityManager.runningAppProcesses ?: emptyList()

            val packageManager = context.packageManager
            val result = mutableListOf<Pair<String, Int>>()

            for (process in processes) {
                try {
                    val appInfo = packageManager.getApplicationInfo(process.processName, 0)
                    val appLabel = packageManager.getApplicationLabel(appInfo).toString()
                    result.add(Pair(appLabel, process.pid))
                } catch (e: Exception) {
                    // App not found, use process name
                    result.add(Pair(process.processName, process.pid))
                }
            }

            result.sortBy { it.first }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error getting running processes", e)
            emptyList()
        }
    }

    /**
     * Get process info from /proc/pid/stat
     */
    fun getProcessInfo(pid: Int): ProcessInfo? {
        return try {
            val statFile = File("/proc/$pid/stat")
            if (!statFile.exists()) return null

            val stat = statFile.readText().split(" ")
            if (stat.size < 3) return null

            ProcessInfo(
                pid = pid,
                name = stat[1].trim('(', ')'),
                state = stat[2],
                ppid = stat[3].toIntOrNull() ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading process info for PID: $pid", e)
            null
        }
    }

    /**
     * Check if process has libgame.so loaded
     */
    fun hasLibraryLoaded(pid: Int, libraryName: String): Boolean {
        return try {
            val mapsFile = File("/proc/$pid/maps")
            if (!mapsFile.exists()) return false

            mapsFile.useLines { lines ->
                lines.any { it.contains(libraryName) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking library for PID: $pid", e)
            false
        }
    }

    /**
     * Kill a process (requires su)
     */
    fun killProcess(pid: Int): Boolean {
        return try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "kill $pid")).waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error killing process", e)
            false
        }
    }

    /**
     * Get memory usage of a process
     */
    fun getMemoryUsage(pid: Int): MemoryInfo? {
        return try {
            val statusFile = File("/proc/$pid/status")
            if (!statusFile.exists()) return null

            var vmPeak = 0L
            var vmSize = 0L
            var vmRss = 0L

            statusFile.forEachLine { line ->
                when {
                    line.startsWith("VmPeak:") -> vmPeak = extractMemoryValue(line)
                    line.startsWith("VmSize:") -> vmSize = extractMemoryValue(line)
                    line.startsWith("VmRSS:") -> vmRss = extractMemoryValue(line)
                }
            }

            MemoryInfo(
                vmPeak = vmPeak,
                vmSize = vmSize,
                vmRss = vmRss
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting memory info for PID: $pid", e)
            null
        }
    }

    private fun extractMemoryValue(line: String): Long {
        return try {
            line.split(Regex("\\s+"))[1].toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    data class ProcessInfo(
        val pid: Int,
        val name: String,
        val state: String,
        val ppid: Int
    )

    data class MemoryInfo(
        val vmPeak: Long,
        val vmSize: Long,
        val vmRss: Long
    )
}