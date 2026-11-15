# LiquidGlass CC — alpha source project

Target: OPPO CPH2781 / ColorOS 16, Android 16.

This is an independent overlay prototype, not an LSPosed module and not a replacement for SystemUI.

## Build
Open this project in Android Studio with JDK 17 and Android SDK 35, or push it to GitHub and run the included Actions workflow. The workflow produces `app-debug.apk`.

## Install and first run
1. Install the APK.
2. Open LiquidGlass CC.
3. Grant "Display over other apps".
4. Grant "Modify system settings" for brightness control.
5. Tap "Launch Liquid Glass Control Center".

## Current alpha limitations
- Wi-Fi, Bluetooth, airplane mode, location and auto-rotate tiles open Android settings panels; Android restricts silent direct toggling by ordinary apps.
- Flashlight and media controls are basic placeholders/initial implementation.
- Blur-behind depends on Android/OEM window blur support and may be disabled by battery/performance settings.
- This prototype does not intercept the system swipe-down gesture. It launches from its app until a separate gesture/quick-launch integration is added.
- No LSPosed hooks or privileged permissions are included.
