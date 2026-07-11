-keep class com.danimodder.dumper.** { *; }
-keep class com.danimodder.dumper.frida.** { *; }
-keep class com.danimodder.dumper.utils.** { *; }
-keep class com.danimodder.dumper.adapter.** { *; }
-keep class com.danimodder.dumper.fragment.** { *; }

-dontwarn com.frida.**
-keep class com.frida.** { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile