package com.danimodder.dumper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danimodder.dumper.R

class InstallFridaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_install_frida, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvContent: TextView = view.findViewById(R.id.tvTutorialContent)
        tvContent.text = """
            📦 INSTALL FRIDA (Untuk ROOT Device)

            Frida adalah dynamic instrumentation toolkit untuk mobile hacking.

            🔹 REQUIREMENTS:
            • Android device dengan ROOT access
            • ADB (Android Debug Bridge)
            • Frida tools di PC

            🔹 LANGKAH-LANGKAH:

            1. INSTALL FRIDA TOOLS DI PC:
               Windows: pip install frida-tools
               Linux/Mac: pip3 install frida-tools

            2. DOWNLOAD FRIDA SERVER:
               • Kunjungi github.com/frida/frida/releases
               • Download frida-server-VERSION-android-arm64.xz
               • Ekstrak file

            3. PUSH KE DEVICE:
               adb push frida-server /data/local/tmp/
               adb shell chmod +x /data/local/tmp/frida-server

            4. START FRIDA SERVER:
               adb shell /data/local/tmp/frida-server

            5. TEST KONEKSI:
               frida-ls-devices
               (Jika berhasil, device akan terlihat)

            ⚠️ CATATAN:
            • Frida memerlukan ROOT access
            • Beberapa game menggunakan anti-cheat yang block Frida
            • Untuk non-root, gunakan Frida Gadget
        """.trimIndent()
    }
}