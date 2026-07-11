package com.danimodder.dumper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.danimodder.dumper.R

class ExtractLibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_extract_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvContent: TextView = view.findViewById(R.id.tvTutorialContent)
        tvContent.text = """
            📁 EXTRACT libgame.so DARI APK

            Aplikasi ini men-dump libgame.so dari memory game yang berjalan. Namun, Anda juga bisa extract dari APK.

            🔹 METHOD 1: EXTRACT DARI APK FILE

            1. DOWNLOAD APK DARI DEVICE:
               • Buka Device File Manager di AIDE
               • Cari APK di /data/app/com.nama.game/
               • Atau gunakan: adb pull /data/app/com.nama.game/

            2. EKSTRAK APK SEPERTI ZIP:
               • Rename .apk menjadi .zip
               • Gunakan WinRAR/7-Zip untuk membuka
               • Navigasi ke: lib/arm64-v8a/ atau lib/armeabi-v7a/
               • Copy libgame.so

            3. VERIFIKASI:
               • File size minimal > 500 KB
               • Magic number: 7F 45 4C 46 (ELF header)

            🔹 METHOD 2: EXTRACT DARI RUNNING PROCESS

            Ini adalah cara yang dilakukan oleh Dani Modder:

            1. JALANKAN GAME
               • Mulai game di device
               • Tunggu hingga fully loaded

            2. BUKA DANI MODDER
               • Pilih proses game dari list
               • Click "Attach"

            3. DUMP LIBRARY
               • App akan cari libgame.so di memory
               • Click "Dump"
               • File akan tersimpan di /sdcard/DaniModder/dump.bin

            ✅ KEUNTUNGAN METHOD 2:
            • Dump dari memory langsung
            • Menangkap runtime code modifications
            • Lebih akurat untuk reversing
        """.trimIndent()
    }
}