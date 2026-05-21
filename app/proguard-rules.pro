# Add project specific ProGuard rules here.

# ── Karoo Extension SDK ──
-keep class io.hammerhead.karooext.** { *; }
-keepclasseswithmembers class * extends io.hammerhead.karooext.extension.DataTypeImpl { *; }
-keepclasseswithmembers class * extends io.hammerhead.karooext.extension.KarooExtension { *; }

# ── kotlinx.serialization ──
# Serialization uses reflection on companion serializer() functions; keep them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their fields
-keepclassmembers class com.karoo.locationtagger.data.** {
    *** Companion;
    <fields>;
}
-keepnames class com.karoo.locationtagger.data.** { *; }

# ── Android components ──
-keep class com.karoo.locationtagger.MainActivity { *; }
-keep class com.karoo.locationtagger.extension.LocationTaggerExtension { *; }
-keep class com.karoo.locationtagger.extension.OpenAppReceiver { *; }
-keep class com.karoo.locationtagger.extension.PoiTagDataType { *; }

# ── ZXing (QR code generation) ──
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**