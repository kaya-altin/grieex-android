# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**

-keep class com.grieex.model.** { *; }


-dontwarn android.util.**
-keep class android.util.**

# loopj
-keep class cz.msebera.android.httpclient.** { *; }
-keep class com.loopj.android.http.** { *; }

# Apache
-dontwarn org.apache.**
-dontnote org.apache.**

-dontwarn com.google.common.**
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keep class org.codehaus.** { *; }
#-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
# public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }
-keep public class your.class.** {
  public void set*(***);
  public *** get*();
}

#-dontwarn info.movito.**
#-keep class info.movito { *; }
#
#-dontwarn com.sburba.tvdbapi.**
#-keep class com.sburba.tvdbapi { *; }
#
#-dontwarn com.uwetrottmann.trakt.v2.**
#-keep class com.uwetrottmann.trakt.v2 { *; }

-dontwarn org.joda.**
-keep class org.joda.**

-dontwarn org.json.**
-keep class org.json.**

-dontwarn org.slf4j.**
-keep class org.slf4j.**


# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }


## GSON 2.2.4 specific rules ##

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Picasso
-dontwarn rx.**

-dontwarn okio.**

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn retrofit.**
#-dontwarn retrofit.appengine.UrlFetchClient
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

