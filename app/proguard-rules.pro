# R8 rules for SmartConvert.
# AndroidX, Material and Room ship their own consumer rules, so no blanket
# -keep rules are needed here (they would disable most shrinking and obfuscation).

# Readable crash reports in Play Console (upload the mapping file with each release)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Manifest components are kept automatically by AAPT2 rules; this covers
# activities referenced only from intents/reflection.
-keep public class * extends android.app.Activity

# Fragments are re-created by the framework via their no-arg constructor
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>();
}

# Strip verbose logging from release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
