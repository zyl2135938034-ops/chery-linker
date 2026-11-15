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

# 小米穿戴 SDK 通过服务名/反射加载内部类，整包保留
-keep class com.xiaomi.xms.wearable.** { *; }

# 桌面组件与手表桥接服务由系统按类名实例化
-keep class com.my.chery.CheryWidget { *; }
-keep class com.my.chery.CheryWidgetSmall { *; }
-keep class com.my.chery.wear.WearBridgeService { *; }