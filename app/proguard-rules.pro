# Add project specific ProGuard rules here.
-keep class com.pdascanner.urovo.** { *; }
-keep class device.scanner.** { *; }
-dontwarn device.scanner.**
-dontwarn org.apache.poi.**
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-dontwarn org.apache.xmlbeans.**
-dontwarn com.fasterxml.**
