package com.danimodder.dumper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danimodder.dumper.R

class UsingAppFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_using_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvContent: TextView = view.findViewById(R.id.tvTutorialContent)
        tvContent.text = """
            🎮 CARA MENGGUNAKAN DANI MODDER

            🔹 PRE-REQUIREMENTS:

            ✓ Device sudah ROOT
            ✓ Frida Server sudah running (adb shell /data/local/tmp/frida-server)
            ✓ Storage permission sudah granted
            ✓ Game sudah dijalankan di background

            🔹 LANGKAH-LANGKAH DUMPING:

            1️⃣ REFRESH PROCESS LIST
               • Click tombol "🔄 Refresh" untuk memuat daftar proses
               • Tunggu beberapa detik
               • List akan menampilkan semua running apps

            2️⃣ PILIH TARGET GAME
               • Scroll ke game yang ingin di-dump
               • Tap untuk pilih
               • Status akan update: "Selected: [Game Name] (PID: xxxxx)"

            3️⃣ ATTACH FRIDA
               • Click tombol "🔗 Attach"
               • App akan cek Frida Server connection
               • Jika berhasil: "✓ Frida attached successfully"
               • App akan auto-search base address libgame.so
               • Status: "✓ libgame.so base address: 0x.......".

            4️⃣ DUMP LIBRARY
               • Tombol "💾 Dump" akan aktif setelah attach berhasil
               • Click untuk mulai dumping
               • Durasi: tergantung ukuran file (biasanya 10-30 detik)
               • Jika selesai: "✓ Dump completed successfully!"

            5️⃣ CEK HASIL
               • File tersimpan di: /sdcard/DaniModder/dump.bin
               • Log tersimpan di: /sdcard/DaniModder/log.txt
               • Buka dengan file manager untuk verify

            🔹 OUTPUT FILES:

            📄 /sdcard/DaniModder/dump.bin
               • Binary dump dari libgame.so
               • Bisa di-analyze dengan: Ghidra, IDA Pro, Binary Ninja

            📄 /sdcard/DaniModder/log.txt
               • Log berisi: PID, base address, timestamp
               • Gunakan untuk reference saat analysis

            ✅ SUCCESS INDICATORS:

            • dump.bin file size > 1 MB
            • log.txt terisi dengan base address
            • Tidak ada error message di log
        """.trimIndent()
    }
}