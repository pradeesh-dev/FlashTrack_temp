-keep class com.flashtrack.app.data.local.entity.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
