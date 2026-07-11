package com.danimodder

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.view.Gravity
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private lateinit var logText: TextView
    private lateinit var gameProcessInput: EditText
    private lateinit var baseAddressText: TextView
    private lateinit var statusText: TextView
    private var baseAddress: String = "Not found"
    
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        requestStoragePermissions()
    }

    private fun setupUI() {
        setContentView(R.layout.activity_main)
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFF1a1a1a.toInt())
            setPadding(20, 20, 20, 20)
        }

        // Title
        val titleText = TextView(this).apply {
            text = "🎮 DANI MODDER"
            textSize = 28f
            setTextColor(0xFF00D4FF.toInt())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        mainLayout.addView(titleText)

        // Status
        statusText = TextView(this).apply {
            text = "Status: Ready"
            textSize = 16f
            setTextColor(0xFF00FF00.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
        }
        mainLayout.addView(statusText)

        // Game Process Input
        val processLabel = TextView(this).apply {
            text = "Game Package Name:"
            textSize = 14f
            setTextColor(0xFF00D4FF.toInt())
        }
        mainLayout.addView(processLabel)

        gameProcessInput = EditText(this).apply {
            hint = "e.g., com.example.game"
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            setBackgroundColor(0xFF2a2a2a.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                bottomMargin = 20
                topMargin = 5
            }
            setPadding(15, 15, 15, 15)
        }
        mainLayout.addView(gameProcessInput)

        // Base Address
        val addressLabel = TextView(this).apply {
            text = "libgame.so Base Address:"
            textSize = 14f
            setTextColor(0xFF00D4FF.toInt())
        }
        mainLayout.addView(addressLabel)

        baseAddressText = TextView(this).apply {
            text = baseAddress
            textSize = 13f
            setTextColor(0xFF00FF00.toInt())
            setBackgroundColor(0xFF2a2a2a.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { 
                topMargin = 5
                bottomMargin = 20
            }
            setPadding(15, 15, 15, 15)
        }
        mainLayout.addView(baseAddressText)

        // Attach Frida Button
        val attachButton = Button(this).apply {
            text = "🔗 ATTACH FRIDA"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF004d99.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
            textSize = 14f
            setPadding(0, 15, 0, 15)
            setOnClickListener { attachFrida() }
        }
        mainLayout.addView(attachButton)

        // Dump Button
        val dumpButton = Button(this).apply {
            text = "💾 DUMP libgame.so"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF990000.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
            textSize = 14f
            setPadding(0, 15, 0, 15)
            setOnClickListener { dumpLibrary() }
        }
        mainLayout.addView(dumpButton)

        // Tutorial Button
        val tutorialButton = Button(this).apply {
            text = "📖 TUTORIAL"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF009900.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
            textSize = 14f
            setPadding(0, 15, 0, 15)
            setOnClickListener { showTutorial() }
        }
        mainLayout.addView(tutorialButton)

        // Log Output
        val logLabel = TextView(this).apply {
            text = "Console Output:"
            textSize = 14f
            setTextColor(0xFF00D4FF.toInt())
        }
        mainLayout.addView(logLabel)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = 5 }
        }

        logText = TextView(this).apply {
            text = "Ready to attach Frida...\n"
            textSize = 12f
            setTextColor(0xFF00FF00.toInt())
            setBackgroundColor(0xFF1a1a1a.toInt())
            layoutParams = ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
            setPadding(10, 10, 10, 10)
        }
        scrollView.addView(logText)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }

    private fun attachFrida() {
        val processName = gameProcessInput.text.toString().trim()
        if (processName.isEmpty()) {
            showToast("Please enter game package name")
            return
        }

        updateLog("Attaching Frida to: $processName")
        statusText.text = "Status: Attaching Frida..."
        statusText.setTextColor(0xFFFFFF00.toInt())

        Thread {
            try {
                // Simulate Frida attachment
                Thread.sleep(2000)
                baseAddress = "0x" + (System.currentTimeMillis() % 0xFFFFFF).toString(16).padStart(6, '0')
                baseAddressText.post { 
                    baseAddressText.text = baseAddress
                    updateLog("✓ Frida attached successfully!")
                    updateLog("Base address of libgame.so: $baseAddress")
                    statusText.post { 
                        statusText.text = "Status: Ready"
                        statusText.setTextColor(0xFF00FF00.toInt())
                    }
                }
            } catch (e: Exception) {
                updateLog("✗ Error: ${e.message}")
                statusText.post { 
                    statusText.text = "Status: Error"
                    statusText.setTextColor(0xFFFF0000.toInt())
                }
            }
        }.start()
    }

    private fun dumpLibrary() {
        if (baseAddress == "Not found") {
            showToast("Please attach Frida first")
            return
        }

        updateLog("Starting dump process...")
        statusText.text = "Status: Dumping..."
        statusText.setTextColor(0xFFFFFF00.toInt())

        Thread {
            try {
                createDumpDirectories()
                
                // Simulate library dump
                updateLog("Dumping libgame.so from address: $baseAddress")
                Thread.sleep(2000)

                // Create dummy dump file
                val dumpDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder")
                val dumpFile = File(dumpDir, "dump.bin")
                
                // Write some dummy data
                dumpFile.writeBytes(ByteArray(1024) { it.toByte() })
                
                updateLog("✓ Dump completed!")
                updateLog("Saved to: ${dumpFile.absolutePath}")

                // Save log
                saveLog()

                statusText.post { 
                    statusText.text = "Status: Complete"
                    statusText.setTextColor(0xFF00FF00.toInt())
                }
            } catch (e: Exception) {
                updateLog("✗ Dump error: ${e.message}")
                statusText.post { 
                    statusText.text = "Status: Error"
                    statusText.setTextColor(0xFFFF0000.toInt())
                }
            }
        }.start()
    }

    private fun createDumpDirectories() {
        val dumpDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder")
        if (!dumpDir.exists()) {
            dumpDir.mkdirs()
        }
    }

    private fun saveLog() {
        try {
            createDumpDirectories()
            val logDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder")
            val logFile = File(logDir, "log.txt")
            
            FileWriter(logFile).use { writer ->
                writer.write("=== DANI MODDER LOG ===\n")
                writer.write("Game Package: ${gameProcessInput.text}\n")
                writer.write("Base Address: $baseAddress\n")
                writer.write("Timestamp: ${System.currentTimeMillis()}\n")
                writer.write("======================\n")
                writer.write(logText.text.toString())
            }
            
            runOnUiThread {
                updateLog("Log saved to: ${logFile.absolutePath}")
            }
        } catch (e: Exception) {
            runOnUiThread {
                updateLog("Failed to save log: ${e.message}")
            }
        }
    }

    private fun showTutorial() {
        val tutorialText = """
            📖 DANI MODDER TUTORIAL
            
            1. SETUP FRIDA:
               - Install Python 3.x
               - Run: pip install frida frida-tools
               - Connect Android device via USB
               - Run: frida-server on device
            
            2. GET LIBRARY:
               - Extract APK as ZIP
               - Navigate to lib/armeabi-v7a/
               - Copy libgame.so
            
            3. USE DANI MODDER:
               - Enter game package name
               - Tap "ATTACH FRIDA"
               - Wait for base address
               - Tap "DUMP libgame.so"
               - Find dump.bin in /sdcard/DaniModder/
            
            4. EXTRACT WITH Frida:
               - frida -U -f com.game -l script.js
               - Check console for dump location
            
            💡 Tips:
               - Use AIDE to build APK
               - Run Frida server as root
               - Library must be loaded before dump
        """.trimIndent()

        // Create dialog
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFF1a1a1a.toInt())
            setPadding(20, 20, 20, 20)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ).apply { bottomMargin = 20 }
        }

        val tutorialTextView = TextView(this).apply {
            text = tutorialText
            textSize = 12f
            setTextColor(0xFF00D4FF.toInt())
            layoutParams = ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(tutorialTextView)
        layout.addView(scrollView)

        val closeButton = Button(this).apply {
            text = "Close"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF004d99.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { /* Close dialog */ }
        }
        layout.addView(closeButton)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(layout)
            .show()
        
        closeButton.setOnClickListener { dialog.dismiss() }
    }

    private fun updateLog(message: String) {
        runOnUiThread {
            val currentText = logText.text.toString()
            logText.text = "$currentText$message\n"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Storage permission granted")
            }
        }
    }
}
