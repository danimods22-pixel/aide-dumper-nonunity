# Dani Modder - Non-Unity Game Dumper

**A professional Android dumper application for non-Unity games**

## 🎮 Features

✅ **Process Attachment** - Attach Frida to running game processes
✅ **Auto Library Detection** - Automatically find libgame.so base address
✅ **Memory Dumping** - Dump native library directly from process memory
✅ **Log Export** - Automatic base address and dump log generation
✅ **Dark UI Theme** - Modern dark theme inspired by Mu'min
✅ **Built-in Tutorial** - Complete guide for installation and usage
✅ **Multi-language Support** - Indonesian and English documentation

## 📋 Requirements

- Android 7.0+ (minSdk: 24)
- **ROOT Access** (required for memory dumping)
- Frida Server running on device
- Storage permissions
- ADB (for initial setup)

## 🚀 Quick Start

### 1. Install Frida Server on Device

```bash
# Install Frida tools on PC
pip install frida-tools

# Download Frida Server
# Visit: https://github.com/frida/frida/releases
# Download: frida-server-VERSION-android-arm64.xz

# Extract and push to device
xz -d frida-server-VERSION-android-arm64.xz
adb push frida-server /data/local/tmp/
adb shell chmod +x /data/local/tmp/frida-server

# Start Frida Server
adb shell /data/local/tmp/frida-server
```

### 2. Build Dani Modder APK

**Using Android Studio:**
```
1. Open project in Android Studio
2. Build → Build Bundle(s) / APK(s)
3. Select APK option
4. Choose Release build
5. Install on device: adb install -r app-release.apk
```

**Using AIDE:**
```
1. Open project in AIDE
2. Menu → Build
3. Select Build APK
4. Install generated APK
```

### 3. Use the App

1. **Grant Storage Permissions**
   - Open app settings
   - Allow storage access

2. **Start Target Game**
   - Launch the game you want to dump
   - Let it fully load

3. **Refresh Process List**
   - Click "🔄 Refresh" button
   - App loads running processes

4. **Select and Attach**
   - Select game from dropdown
   - Click "🔗 Attach" button
   - Wait for base address detection

5. **Dump Library**
   - Click "💾 Dump" button
   - Wait for dump to complete
   - Check `/sdcard/DaniModder/dump.bin`

## 📁 Output Files

```
/sdcard/DaniModder/
├── dump.bin          # Binary dump of libgame.so
└── log.txt          # Dump information and base address
```

## 🛠️ Project Structure

```
DaniModder/
├── app/
│   ├── src/main/
│   │   ├── java/com/danimodder/dumper/
│   │   │   ├── MainActivity.kt
│   │   │   ├── TutorialActivity.kt
│   │   │   ├── frida/
│   │   │   │   └── FridaManager.kt
│   │   │   ├── utils/
│   │   │   │   ├── ProcessManager.kt
│   │   │   │   └── StorageManager.kt
│   │   │   ├── adapter/
│   │   │   │   └── TutorialPagerAdapter.kt
│   │   │   └── fragment/
│   │   │       ├── InstallFridaFragment.kt
│   │   │       ├── ExtractLibraryFragment.kt
│   │   │       ├── UsingAppFragment.kt
│   │   │       └── TroubleshootingFragment.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 🔧 Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Key Libraries**:
  - Frida (Android Bindings)
  - AndroidX & Material Design
  - Coroutines
  - ViewPager2

## 🎓 Tutorial Sections

### Tab 1: Install Frida
- Step-by-step Frida installation
- Device setup procedures
- Troubleshooting Frida connection

### Tab 2: Extract libgame.so
- APK extraction method
- Process memory dumping
- File verification

### Tab 3: Using the App
- Detailed usage walkthrough
- Process selection guide
- Dump verification steps

### Tab 4: Troubleshooting
- Common errors and solutions
- FAQ section
- Safety and ethics guidelines

## ⚙️ Permissions

```xml
<!-- Required -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- For process access -->
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.GET_PROCESS_STATE_AND_OOM_SCORE" />

<!-- Optional for su access -->
<!-- <uses-permission android:name="android.permission.ACCESS_SUPERUSER" /> -->
```

## 📝 Troubleshooting

### "Permission Denied"
- Grant storage permissions in settings
- For Android 11+, need MANAGE_EXTERNAL_STORAGE

### "Frida not attached"
- Check Frida Server running: `adb shell ps | grep frida`
- Start if needed: `adb shell /data/local/tmp/frida-server &`
- Wait 2-3 seconds before attaching

### "libgame.so not found"
- Ensure game fully loaded
- Check game has native library: `adb shell pm dump com.game.name | grep native`
- Only works with NON-UNITY games

### "Memory dump failed"
- Device must be ROOT
- Verify: `adb shell su -c "whoami"` (should return "root")

## 🛡️ Legal & Ethics

⚠️ **This tool is for:**
✅ Educational purposes
✅ Personal game modding
✅ Reverse engineering learning
✅ Non-commercial analysis

❌ **NOT for:**
- Game cracking or piracy
- Cheat development
- Commercial exploitation
- Distributing copyrighted content

**Always comply with game Terms of Service and local laws.**

## 🤝 Support

For issues or questions:
1. Check tutorial sections in app
2. Review troubleshooting FAQ
3. Check project documentation
4. Create GitHub issue with:
   - Error message
   - Device info (model, Android version)
   - Reproduction steps

## 📄 License

This project is provided for educational purposes.

---

**Created by: Dani Modder**
**Version: 1.0.0**
**Last Updated: 2024**

*"Knowledge is power, use it wisely."*