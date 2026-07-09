# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Type-safe Navigation-Compose routes (Routes.kt) rely on kotlinx.serialization
# to encode/decode arguments via each Route's serializer(), looked up in a way
# R8 can't always trace statically — minify has documented cases of stripping
# or renaming these and breaking navigation. Same risk for any other
# @Serializable class if R8 decides a serializer() reference isn't reachable.
# Added proactively (not yet confirmed against a real assembleRelease run).
-keep,includedescriptorclasses class com.davidbrazuna.pokemonapp.**$$serializer { *; }
-keepclassmembers class com.davidbrazuna.pokemonapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.davidbrazuna.pokemonapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}