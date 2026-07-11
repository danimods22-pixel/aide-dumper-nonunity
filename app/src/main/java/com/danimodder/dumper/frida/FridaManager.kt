package com.danimodder.dumper.frida

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class FridaResult(
    val success: Boolean,
    val message: String = "",
    val data: Any? = null
)

class FridaManager {

    companion object {
        private const val TAG = "FridaManager"
        private const val FRIDA_SERVER_PORT = 27042
    }

    /**
     * Attach Frida to a specific process
     */
    fun attachToProcess(pid: Int, context: Context): FridaResult {
        return try {
            // Check if Frida server is available
            if (!isFridaServerRunning()) {
                // Try to start Frida server
                startFridaServer(context)
            }

            Log.d(TAG, "Attempting to attach to PID: $pid")
            
            // Use Runtime.exec to interact with Frida
            val process = Runtime.getRuntime().exec("frida -p $pid --no-pause")
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                FridaResult(
                    success = true,
                    message = "Successfully attached to process $pid"
                )
            } else {
                FridaResult(
                    success = false,
                    message = "Frida attach failed with exit code $exitCode"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching to process", e)
            FridaResult(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Find libgame.so base address in process memory
     */
    fun findLibraryBaseAddress(pid: Int, libraryName: String): Long {
        return try {
            val mapsFile = File("/proc/$pid/maps")
            if (!mapsFile.exists()) {
                Log.e(TAG, "Maps file not found for PID: $pid")
                return 0L
            }

            val baseAddress = mapsFile.useLines { lines ->
                lines.firstOrNull { line ->
                    line.contains(libraryName) && line.contains("r-xp")
                }?.let { line ->
                    val parts = line.split("-")
                    if (parts.isNotEmpty()) {
                        try {
                            parts[0].toLong(16)
                        } catch (e: NumberFormatException) {
                            0L
                        }
                    } else {
                        0L
                    }
                } ?: 0L
            }

            Log.d(TAG, "$libraryName base address: 0x${baseAddress.toString(16)}")
            baseAddress
        } catch (e: Exception) {
            Log.e(TAG, "Error finding library base address", e)
            0L
        }
    }

    /**
     * Dump library memory
     */
    fun dumpLibrary(pid: Int, baseAddress: Long): FridaResult {
        return try {
            Log.d(TAG, "Starting dump from base address: 0x${baseAddress.toString(16)}")

            // Create output directory
            val outputDir = File("/sdcard/DaniModder")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val outputFile = File(outputDir, "dump.bin")

            // Use /proc/pid/mem to dump memory
            val result = dumpMemoryUsingProc(pid, baseAddress, outputFile)

            if (result.success) {
                FridaResult(
                    success = true,
                    message = "Dump successful",
                    data = outputFile.absolutePath
                )
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dumping library", e)
            FridaResult(
                success = false,
                message = e.message ?: "Dump failed"
            )
        }
    }

    /**
     * Dump memory using /proc/pid/mem
     */
    private fun dumpMemoryUsingProc(pid: Int, baseAddress: Long, outputFile: File): FridaResult {
        return try {
            // Get library size from maps
            val mapsFile = File("/proc/$pid/maps")
            val memFile = File("/proc/$pid/mem")

            if (!memFile.exists() || !mapsFile.exists()) {
                return FridaResult(
                    success = false,
                    message = "Cannot access process memory"
                )
            }

            var librarySize = 0L
            mapsFile.useLines { lines ->
                lines.filter { line -> 
                    line.contains("libgame.so") && line.contains("r-xp")
                }.forEach { line ->
                    try {
                        val parts = line.split(" ").filter { it.isNotEmpty() }
                        if (parts.size >= 1) {
                            val rangeParts = parts[0].split("-")
                            if (rangeParts.size == 2) {
                                val start = rangeParts[0].toLong(16)
                                val end = rangeParts[1].toLong(16)
                                librarySize = end - start
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing maps", e)
                    }
                }
            }

            if (librarySize == 0L) {
                return FridaResult(
                    success = false,
                    message = "Could not determine library size"
                )
            }

            // Dump using dd command
            val command = "dd if=/proc/$pid/mem of=${outputFile.absolutePath} bs=1 skip=$baseAddress count=$librarySize"
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            process.waitFor()

            if (outputFile.exists() && outputFile.length() > 0) {
                FridaResult(
                    success = true,
                    message = "Dump successful",
                    data = outputFile.absolutePath
                )
            } else {
                FridaResult(
                    success = false,
                    message = "Dump file was not created"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in dumpMemoryUsingProc", e)
            FridaResult(
                success = false,
                message = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Check if Frida server is running
     */
    private fun isFridaServerRunning(): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress("127.0.0.1", FRIDA_SERVER_PORT), 1000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Try to start Frida server (requires root)
     */
    private fun startFridaServer(context: Context): Boolean {
        return try {
            val fridaPath = "/system/bin/frida-server"
            val file = File(fridaPath)
            
            if (file.exists()) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "$fridaPath &"))
                Thread.sleep(2000) // Wait for server to start
                true
            } else {
                Log.w(TAG, "Frida server not found at $fridaPath")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Frida server", e)
            false
        }
    }
}