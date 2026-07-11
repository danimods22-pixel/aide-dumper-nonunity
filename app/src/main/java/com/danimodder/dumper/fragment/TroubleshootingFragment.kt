package com.danimodder.dumper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danimodder.dumper.R

class TroubleshootingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_troubleshooting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvContent: TextView = view.findViewById(R.id.tvTutorialContent)
        tvContent.text = """
            🔧 TROUBLESHOOTING & FAQ

            ❌ ERROR: "Permission Denied"
            ✓ Solution:
              • Check storage permissions in app settings
              • Grant "Allow storage access"
              • Pada Android 11+, perlu MANAGE_EXTERNAL_STORAGE

            ❌ ERROR: "Frida not attached"
            ✓ Solution:
              • Pastikan Frida Server running: adb shell ps | grep frida
              • Jika tidak: adb shell /data/local/tmp/frida-server &
              • Tunggu 2-3 detik sebelum attach

            ❌ ERROR: "libgame.so not found"
            ✓ Solution:
              • Pastikan game sudah fully loaded
              • Verifi game pakai native library dengan:
                adb shell pm dump com.nama.game | grep native
              • Target hanya game NON-UNITY
              • Jika unity game, gunakan dumper lain

            ❌ ERROR: "Memory dump failed"
            ✓ Solution:
              • Device harus sudah ROOT
              • Cek: adb shell su -c "whoami"
              • Output: root (bukan: command not found)
              • Jika belum root, root device dulu

            ❌ ERROR: "Process terminated"
            ✓ Solution:
              • Game mungkin crash
              • Restart game, jangan close sebelum dump selesai
              • Disable auto-close in game settings

            ❓ FAQ: Bisa untuk game Unity?
            ✗ Tidak. Tool ini khusus NON-UNITY
            • Game Unity pakai IL2CPP (libil2cpp.so)
            • Gunakan: Il2CppDumper atau APKUtil

            ❓ FAQ: Bisa tanpa ROOT?
            ✗ Dump dari /proc/pid/mem memerlukan ROOT
            • Alternatif: Extract dari APK file
            • Atau gunakan Frida Gadget (lebih rumit)

            ❓ FAQ: Dump file besar banget?
            ✓ Normal, sebesar ukuran libgame.so
            • Rata-rata: 20-200 MB
            • Pastikan storage cukup

            ❓ FAQ: Hasil dump corrupt?
            ✓ Biasanya bukan corrupt:
              • Verify dengan: file dump.bin
              • Output: ELF 64-bit LSB shared object
              • Jika mau double-check:
                hexdump -C dump.bin | head
                (harus terlihat: 7f 45 4c 46 = ELF magic)

            ❓ FAQ: Mau upload kemana hasil dump?
            • GitHub Gist (private)
            • GitLab
            • Personal server dengan SSL
            • Jangan public di forum

            🛡️ SAFETY & ETHICS:

            ⚠️ Tool ini untuk:
            ✓ Educational purposes
            ✓ Game modding sendiri
            ✓ Reversing engineering learning
            ✗ Distribusi game crack/piracy
            ✗ Cheat development
            ✗ Illegal purposes

            📜 Gunakan tool ini dengan bijak & comply with ToS game.
        """.trimIndent()
    }
}