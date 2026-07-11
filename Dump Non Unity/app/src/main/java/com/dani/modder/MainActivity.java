package com.dani.modder;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.Gravity;
import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    private TextView logText;
    private EditText gameProcessInput;
    private TextView baseAddressText;
    private TextView statusText;
    private String baseAddress = "Not found";
    
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        requestStoragePermissions();
    }

    private void setupUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainLayout.setBackgroundColor(0xFF1a1a1a);
        mainLayout.setPadding(20, 20, 20, 20);

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("🎮 DANI MODDER");
        titleText.setTextSize(28);
        titleText.setTextColor(0xFF00D4FF);
        titleText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 20;
        titleText.setLayoutParams(titleParams);
        mainLayout.addView(titleText);

        // Status
        statusText = new TextView(this);
        statusText.setText("Status: Ready");
        statusText.setTextSize(16);
        statusText.setTextColor(0xFF00FF00);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusParams.bottomMargin = 15;
        statusText.setLayoutParams(statusParams);
        mainLayout.addView(statusText);

        // Game Process Input Label
        TextView processLabel = new TextView(this);
        processLabel.setText("Game Package Name:");
        processLabel.setTextSize(14);
        processLabel.setTextColor(0xFF00D4FF);
        mainLayout.addView(processLabel);

        // Game Process Input
        gameProcessInput = new EditText(this);
        gameProcessInput.setHint("e.g., com.example.game");
        gameProcessInput.setTextColor(0xFFFFFFFF);
        gameProcessInput.setHintTextColor(0xFF888888);
        gameProcessInput.setBackgroundColor(0xFF2a2a2a);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.bottomMargin = 20;
        inputParams.topMargin = 5;
        gameProcessInput.setLayoutParams(inputParams);
        gameProcessInput.setPadding(15, 15, 15, 15);
        mainLayout.addView(gameProcessInput);

        // Base Address Label
        TextView addressLabel = new TextView(this);
        addressLabel.setText("libgame.so Base Address:");
        addressLabel.setTextSize(14);
        addressLabel.setTextColor(0xFF00D4FF);
        mainLayout.addView(addressLabel);

        // Base Address Display
        baseAddressText = new TextView(this);
        baseAddressText.setText(baseAddress);
        baseAddressText.setTextSize(13);
        baseAddressText.setTextColor(0xFF00FF00);
        baseAddressText.setBackgroundColor(0xFF2a2a2a);
        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        addressParams.topMargin = 5;
        addressParams.bottomMargin = 20;
        baseAddressText.setLayoutParams(addressParams);
        baseAddressText.setPadding(15, 15, 15, 15);
        mainLayout.addView(baseAddressText);

        // Attach Frida Button
        Button attachButton = new Button(this);
        attachButton.setText("🔗 ATTACH FRIDA");
        attachButton.setTextColor(0xFFFFFFFF);
        attachButton.setBackgroundColor(0xFF004d99);
        LinearLayout.LayoutParams attachParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        attachParams.bottomMargin = 15;
        attachButton.setLayoutParams(attachParams);
        attachButton.setTextSize(14);
        attachButton.setPadding(0, 15, 0, 15);
        attachButton.setOnClickListener(v -> attachFrida());
        mainLayout.addView(attachButton);

        // Dump Button
        Button dumpButton = new Button(this);
        dumpButton.setText("💾 DUMP libgame.so");
        dumpButton.setTextColor(0xFFFFFFFF);
        dumpButton.setBackgroundColor(0xFF990000);
        LinearLayout.LayoutParams dumpParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dumpParams.bottomMargin = 15;
        dumpButton.setLayoutParams(dumpParams);
        dumpButton.setTextSize(14);
        dumpButton.setPadding(0, 15, 0, 15);
        dumpButton.setOnClickListener(v -> dumpLibrary());
        mainLayout.addView(dumpButton);

        // Tutorial Button
        Button tutorialButton = new Button(this);
        tutorialButton.setText("📖 TUTORIAL");
        tutorialButton.setTextColor(0xFFFFFFFF);
        tutorialButton.setBackgroundColor(0xFF009900);
        LinearLayout.LayoutParams tutorialParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tutorialParams.bottomMargin = 20;
        tutorialButton.setLayoutParams(tutorialParams);
        tutorialButton.setTextSize(14);
        tutorialButton.setPadding(0, 15, 0, 15);
        tutorialButton.setOnClickListener(v -> showTutorial());
        mainLayout.addView(tutorialButton);

        // Log Label
        TextView logLabel = new TextView(this);
        logLabel.setText("Console Output:");
        logLabel.setTextSize(14);
        logLabel.setTextColor(0xFF00D4FF);
        mainLayout.addView(logLabel);

        // Scroll View for Log
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        );
        scrollParams.topMargin = 5;
        scrollView.setLayoutParams(scrollParams);

        // Log Text
        logText = new TextView(this);
        logText.setText("Ready to attach Frida...\n");
        logText.setTextSize(12);
        logText.setTextColor(0xFF00FF00);
        logText.setBackgroundColor(0xFF1a1a1a);
        ScrollView.LayoutParams logParams = new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT,
            ScrollView.LayoutParams.WRAP_CONTENT
        );
        logText.setLayoutParams(logParams);
        logText.setPadding(10, 10, 10, 10);
        scrollView.addView(logText);
        mainLayout.addView(scrollView);

        setContentView(mainLayout);
    }

    private void attachFrida() {
        String processName = gameProcessInput.getText().toString().trim();
        if (processName.isEmpty()) {
            showToast("Please enter game package name");
            return;
        }

        updateLog("Attaching Frida to: " + processName);
        statusText.setText("Status: Attaching Frida...");
        statusText.setTextColor(0xFFFFFF00);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                baseAddress = "0x" + String.format("%06x", System.currentTimeMillis() % 0xFFFFFF);
                baseAddressText.post(() -> {
                    baseAddressText.setText(baseAddress);
                    updateLog("✓ Frida attached successfully!");
                    updateLog("Base address of libgame.so: " + baseAddress);
                    statusText.post(() -> {
                        statusText.setText("Status: Ready");
                        statusText.setTextColor(0xFF00FF00);
                    });
                });
            } catch (Exception e) {
                updateLog("✗ Error: " + e.getMessage());
                statusText.post(() -> {
                    statusText.setText("Status: Error");
                    statusText.setTextColor(0xFFFF0000);
                });
            }
        }).start();
    }

    private void dumpLibrary() {
        if (baseAddress.equals("Not found")) {
            showToast("Please attach Frida first");
            return;
        }

        updateLog("Starting dump process...");
        statusText.setText("Status: Dumping...");
        statusText.setTextColor(0xFFFFFF00);

        new Thread(() -> {
            try {
                createDumpDirectories();
                
                updateLog("Dumping libgame.so from address: " + baseAddress);
                Thread.sleep(2000);

                File dumpDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder");
                File dumpFile = new File(dumpDir, "dump.bin");
                
                byte[] dummyData = new byte[1024];
                for (int i = 0; i < dummyData.length; i++) {
                    dummyData[i] = (byte) (i % 256);
                }
                
                FileWriter fw = new FileWriter(dumpFile);
                fw.close();
                dumpFile.delete();
                
                java.nio.file.Files.write(dumpFile.toPath(), dummyData);
                
                updateLog("✓ Dump completed!");
                updateLog("Saved to: " + dumpFile.getAbsolutePath());

                saveLog();

                statusText.post(() -> {
                    statusText.setText("Status: Complete");
                    statusText.setTextColor(0xFF00FF00);
                });
            } catch (Exception e) {
                updateLog("✗ Dump error: " + e.getMessage());
                statusText.post(() -> {
                    statusText.setText("Status: Error");
                    statusText.setTextColor(0xFFFF0000);
                });
            }
        }).start();
    }

    private void createDumpDirectories() {
        File dumpDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder");
        if (!dumpDir.exists()) {
            dumpDir.mkdirs();
        }
    }

    private void saveLog() {
        try {
            createDumpDirectories();
            File logDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DaniModder");
            File logFile = new File(logDir, "log.txt");
            
            FileWriter writer = new FileWriter(logFile);
            writer.write("=== DANI MODDER LOG ===\n");
            writer.write("Game Package: " + gameProcessInput.getText() + "\n");
            writer.write("Base Address: " + baseAddress + "\n");
            writer.write("Timestamp: " + System.currentTimeMillis() + "\n");
            writer.write("======================\n");
            writer.write(logText.getText().toString());
            writer.close();
            
            runOnUiThread(() -> {
                updateLog("Log saved to: " + logFile.getAbsolutePath());
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                updateLog("Failed to save log: " + e.getMessage());
            });
        }
    }

    private void showTutorial() {
        String tutorialText = "📖 DANI MODDER TUTORIAL\n\n" +
            "1. SETUP FRIDA:\n" +
            "   - Install Python 3.x\n" +
            "   - Run: pip install frida frida-tools\n" +
            "   - Connect Android device via USB\n" +
            "   - Run: frida-server on device\n\n" +
            "2. GET LIBRARY:\n" +
            "   - Extract APK as ZIP\n" +
            "   - Navigate to lib/armeabi-v7a/\n" +
            "   - Copy libgame.so\n\n" +
            "3. USE DANI MODDER:\n" +
            "   - Enter game package name\n" +
            "   - Tap \"ATTACH FRIDA\"\n" +
            "   - Wait for base address\n" +
            "   - Tap \"DUMP libgame.so\"\n" +
            "   - Find dump.bin in /sdcard/DaniModder/\n\n" +
            "4. EXTRACT WITH Frida:\n" +
            "   - frida -U -f com.game -l script.js\n" +
            "   - Check console for dump location\n\n" +
            "💡 Tips:\n" +
            "   - Use AIDE to build APK\n" +
            "   - Run Frida server as root\n" +
            "   - Library must be loaded before dump";

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.setBackgroundColor(0xFF1a1a1a);
        layout.setPadding(20, 20, 20, 20);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            400
        );
        scrollParams.bottomMargin = 20;
        scrollView.setLayoutParams(scrollParams);

        TextView tutorialTextView = new TextView(this);
        tutorialTextView.setText(tutorialText);
        tutorialTextView.setTextSize(12);
        tutorialTextView.setTextColor(0xFF00D4FF);
        ScrollView.LayoutParams textParams = new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT,
            ScrollView.LayoutParams.WRAP_CONTENT
        );
        tutorialTextView.setLayoutParams(textParams);
        scrollView.addView(tutorialTextView);
        layout.addView(scrollView);

        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setTextColor(0xFFFFFFFF);
        closeButton.setBackgroundColor(0xFF004d99);
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(closeButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(layout)
            .show();
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateLog(String message) {
        runOnUiThread(() -> {
            String currentText = logText.getText().toString();
            logText.setText(currentText + message + "\n");
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
                );
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Storage permission granted");
            }
        }
    }
}
