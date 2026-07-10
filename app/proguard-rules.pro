# Keep TetheringManager classes for reflection-based access
-keep class android.net.TetheringManager { *; }
-keep class android.net.TetheringManager$* { *; }

# Keep Bluetooth classes
-keep class android.bluetooth.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
