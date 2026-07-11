package com.danimodder.dumper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.danimodder.dumper.databinding.ActivityMainBinding
import com.danimodder.dumper.frida.FridaManager
import com.danimodder.dumper.utils.ProcessManager
import com.danimodder.dumper.utils.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fridaManager = FridaManager()
    private val processManager = ProcessManager()
    private val storageManager = StorageManager()
    
    private var selectedProcess: String? = null
    private var selectedPid: Int = -1
    private var libgameBaseAddress: Long = 0L

    companion object {
        private const val TAG = "DaniModder"
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request permissions
        checkAndRequestPermissions()

        // Initialize UI
        setupUI()
        loadProcessList()
    }

    private fun setupUI() {
        // Load processes button
        binding.btnRefresh.setOnClickListener {
            loadProcessList()
        }

        // Attach button
        binding.btnAttach.setOnClickListener {
            attachToProcess()
        }

        // Dump button
        binding.btnDump.setOnClickListener {
            dumpLibrary()
        }

        // Tutorial button
        binding.btnTutorial.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        // Process list spinner
        binding.spinnerProcesses.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    if (position >= 0) {
                        val process = parent?.getItemAtPosition(position) as? String
                        process?.let {
                            val parts = it.split(" - PID: ")
                            if (parts.size == 2) {
                                selectedProcess = parts[0]
                                selectedPid = parts[1].toIntOrNull() ?: -1
                                updateStatusLog("Selected: $selectedProcess (PID: $selectedPid)")
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    selectedProcess = null
                    selectedPid = -1
                }
            }
        )
    }

    private fun loadProcessList() {
        lifecycleScope.launch {
            try {
                updateStatusLog("Loading processes...")
                val processes = withContext(Dispatchers.Default) {
                    processManager.getRunningProcesses(this@MainActivity)
                }

                val processNames = processes.map { "${it.first} - PID: ${it.second}" }
                val adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    processNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerProcesses.adapter = adapter

                updateStatusLog("Loaded ${processes.size} processes")
            } catch (e: Exception) {
                updateStatusLog("Error loading processes: ${e.message}")
                Log.e(TAG, "Error loading processes", e)
            }
        }
    }

    private fun attachToProcess() {
        if (selectedPid == -1) {
            showToast("Please select a process first!")
            return
        }

        lifecycleScope.launch {
            try {
                updateStatusLog("Attaching Frida to PID: $selectedPid...")
                val result = withContext(Dispatchers.Default) {
                    fridaManager.attachToProcess(selectedPid, this@MainActivity)
                }

                if (result.success) {
                    updateStatusLog("✓ Frida attached successfully to $selectedProcess")
                    
                    // Find libgame.so base address
                    updateStatusLog("Searching for libgame.so base address...")
                    val baseAddress = withContext(Dispatchers.Default) {
                        fridaManager.findLibraryBaseAddress(selectedPid, "libgame.so")
                    }

                    if (baseAddress > 0) {
                        libgameBaseAddress = baseAddress
                        updateStatusLog("✓ libgame.so base address: 0x${baseAddress.toString(16).uppercase()}")
                        binding.btnDump.isEnabled = true
                    } else {
                        updateStatusLog("✗ Could not find libgame.so in process memory")
                        binding.btnDump.isEnabled = false
                    }
                } else {
                    updateStatusLog("✗ Failed to attach: ${result.message}")
                    binding.btnDump.isEnabled = false
                }
            } catch (e: Exception) {
                updateStatusLog("Error attaching Frida: ${e.message}")
                Log.e(TAG, "Error attaching Frida", e)
                binding.btnDump.isEnabled = false
            }
        }
    }

    private fun dumpLibrary() {
        if (selectedPid == -1 || libgameBaseAddress == 0L) {
            showToast("Please attach to a process first!")
            return
        }

        lifecycleScope.launch {
            try {
                updateStatusLog("Starting dump from libgame.so...")
                binding.btnDump.isEnabled = false

                val dumpResult = withContext(Dispatchers.Default) {
                    fridaManager.dumpLibrary(selectedPid, libgameBaseAddress)
                }

                if (dumpResult.success) {
                    val outputPath = dumpResult.data as? String ?: "/sdcard/DaniModder/dump.bin"
                    updateStatusLog("✓ Dump completed successfully!")
                    updateStatusLog("Location: $outputPath")

                    // Save log
                    val logContent = """
                        === DANI MODDER DUMP LOG ===
                        Timestamp: ${System.currentTimeMillis()}
                        Process: $selectedProcess
                        PID: $selectedPid
                        Library: libgame.so
                        Base Address: 0x${libgameBaseAddress.toString(16).uppercase()}
                        Dump Path: $outputPath
                        Status: SUCCESS
                    """.trimIndent()

                    withContext(Dispatchers.Default) {
                        storageManager.saveDumpLog(logContent)
                    }

                    updateStatusLog("✓ Log saved to /sdcard/DaniModder/log.txt")
                    showToast("Dump completed! Check /sdcard/DaniModder/")
                } else {
                    updateStatusLog("✗ Dump failed: ${dumpResult.message}")
                    showToast("Dump failed: ${dumpResult.message}")
                }
            } catch (e: Exception) {
                updateStatusLog("Error during dump: ${e.message}")
                Log.e(TAG, "Error during dump", e)
                showToast("Error: ${e.message}")
            } finally {
                binding.btnDump.isEnabled = libgameBaseAddress > 0
            }
        }
    }

    private fun updateStatusLog(message: String) {
        runOnUiThread {
            val currentText = binding.tvLog.text.toString()
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val newText = "[$timestamp] $message\n$currentText"
            binding.tvLog.text = newText.take(2000) // Limit log size
            Log.d(TAG, message)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requiredPermissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadProcessList()
            } else {
                showToast("Permissions denied!")
            }
        }
    }
}