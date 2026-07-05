# JNI bridge — native methods resolved by name.
-keep class dev.ffmpegkit.tesseract.TesseractJNI { *; }
-keepclasseswithmembernames class * { native <methods>; }
# Public API
-keep public class dev.ffmpegkit.tesseract.** { public *; }
