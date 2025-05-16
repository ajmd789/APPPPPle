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

# 保留基本配置
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions

# 保留 XML 解析器相关类
-keep class org.xmlpull.** { *; }
-keep class org.kxml2.** { *; }
-dontwarn org.xmlpull.**
-dontwarn org.kxml2.**
-dontwarn android.content.res.XmlResourceParser

# 保留自定义的模型类
-keep class com.example.appppple.domain.model.** { *; }
-keep class com.example.appppple.data.model.** { *; }

# 保留 Room 相关类
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# 保留 Gson 相关类
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 Epublib 相关类
-keep class nl.siegmann.epublib.** { *; }
-dontwarn nl.siegmann.epublib.**

# 保留 jsoup 相关类
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# 保留 juniversalchardet 相关类
-keep class org.mozilla.universalchardet.** { *; }
-dontwarn org.mozilla.universalchardet.**

# 保留 ViewModel 相关类
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# 保留 Parcelable 相关类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 相关类
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留自定义 View 的 get 和 set 方法
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 R8 完全模式
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile