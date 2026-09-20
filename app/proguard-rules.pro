# CalcNova release shrinking rules.
# Minification is OFF by default (see app/build.gradle.kts). When you turn
# isMinifyEnabled / isShrinkResources on for a Play Store release build,
# these keep rules protect the pieces that reflection-based libraries need.

# kotlinx.serialization: keep generated serializers and @Serializable classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.calcnova.app.**$$serializer { *; }
-keepclassmembers class com.calcnova.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.calcnova.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Compose compiler already keeps what it needs via consumer rules bundled
# in the library AARs - no manual Compose rules required here.
