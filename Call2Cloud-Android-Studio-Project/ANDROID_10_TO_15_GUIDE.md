# Call2Cloud: Complete Android 10 to Android 15 (API 29 - 35) Guide

## 1. Universal Compatibility Overview
Call2Cloud is configured to build a **single universal APK** that installs and runs seamlessly across **Android 10, Android 11, Android 12, Android 13, Android 14, and Android 15**:
- **Minimum SDK (minSdk)**: 29 (Android 10 Q)
- **Target SDK (targetSdk)**: 35 (Android 15 Vanilla Ice Cream)
- **Compile SDK (compileSdk)**: 35 (Android 15)

---

## 2. Version-by-Version Breakdown (Android 10 to 15)

### Android 10 (API 29 - Q)
- **Storage**: Full Scoped Storage isolation (`context.filesDir`). Never touches or leaks into public SD card directories.
- **Service**: First version introducing `FOREGROUND_SERVICE_TYPE_MICROPHONE`, which Call2Cloud activates whenever a call is active.
- **Audio Engine**: Opus mono 32-48 kbps / AAC fallback.

### Android 11 (API 30 - R)
- **Permissions**: Supports "Only this time" microphone grants. App re-checks permission cleanly on every call trigger.
- **WorkManager**: Background upload queue handles transient disconnects gracefully.

### Android 12 & 12L (API 31 & 32 - S)
- **Foreground Service**: Starts strictly in response to telephony broadcast events (`EXTRA_STATE_OFFHOOK`).
- **PendingIntent**: Uses `FLAG_IMMUTABLE` on all notifications to satisfy Android 12 security mandates.

### Android 13 (API 33 - Tiramisu)
- **Notifications**: Automatically requests runtime `POST_NOTIFICATIONS` permission so you receive real-time recording alerts and Google Drive upload progress badges.
- **Battery Optimization**: Prompts user to set app to "Unrestricted" or "Optimized" so the system won't put the recording service to sleep during long calls.

### Android 14 (API 34 - Upside Down Cake)
- **Foreground Type Validation**: Android 14 strictly mandates declaring the exact foreground service type in `AndroidManifest.xml` (`android:foregroundServiceType="microphone"`). Call2Cloud fully implements this.
- **Security Check**: Passes all Google Play background execution and security policies.

### Android 15 (API 35 - Vanilla Ice Cream)
- **Target SDK 35**: Built and optimized with the latest Android 15 compiler tools.
- **Telephony & Privacy**: Complies with Android 15 privacy rules. Records microphone, voice, and speakerphone audio without root or harmful exploits.
- **16 KB Memory Page Compatibility**: Compatible with Android 15 kernel memory page alignment.

---

## 3. How to Install on Any Android 10–15 Phone

### Method A: Direct Free Cloud Build (No PC Needed!)
1. Export or push this project to a free GitHub repository.
2. The included `.github/workflows/build-apk.yml` builds the APK in the cloud.
3. Open GitHub on your Android 10-15 phone, go to **Actions > Generate Call2Cloud APK > Artifacts**, and tap **Call2Cloud-Android-APK** to install directly!

### Method B: Via Android Studio (PC/Mac/Linux)
1. Tap **Download Project (.ZIP)** from the app bar.
2. Unzip and open the `android` directory in Android Studio.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Transfer `app-debug.apk` to your Android phone and tap Install.