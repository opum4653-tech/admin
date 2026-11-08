# Add project specific ProGuard rules here.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class com.andorid.ninmods.admin.data.model.** { *; }
