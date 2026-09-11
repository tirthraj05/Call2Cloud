# Call2Cloud APK Build Guide

This guide provides step-by-step instructions to build an APK file for the Call2Cloud Android application.

## Prerequisites

- **Android Studio** (latest version recommended)
- **Java Development Kit (JDK)** 17 or higher
- **Android SDK** with API level 35 (Android 15) installed
- **Gradle** (included with Android Studio)
- **Minimum 2GB free disk space**

## Build Methods

### Method 1: Using Android Studio (Recommended for Beginners)

1. **Open the Project**
   - Launch Android Studio
   - Click `File` → `Open`
   - Navigate to `Call2Cloud-Android-Studio-Project/android/`
   - Click `Open`

2. **Wait for Gradle Sync**
   - Android Studio will automatically sync the Gradle files
   - Wait for the indexing to complete (watch the status bar)

3. **Configure Build Variant**
   - In Android Studio, select `Build` → `Select Build Variant`
   - Choose `release` variant (for production APK)
   - Or choose `debug` variant (for testing)

4. **Build the APK**
   - Go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Wait for the build process to complete
   - You'll see a notification when the build is successful

5. **Locate the APK**
   - The APK will be generated at:
     ```
     Call2Cloud-Android-Studio-Project/android/app/build/outputs/apk/release/app-release.apk
     ```
     (or `debug/app-debug.apk` for debug builds)

6. **Deploy to Device**
   - Connect your Android device (Android 10-15)
   - Enable Developer Mode: Settings → About Phone → tap Build Number 7 times
   - Enable USB Debugging: Settings → Developer Options → USB Debugging
   - Click `Run` → `Run 'app'` or drag the APK to the device

### Method 2: Using Gradle Command Line

1. **Navigate to Project Directory**
   ```bash
   cd Call2Cloud-Android-Studio-Project/android
   ```

2. **Make Gradle Wrapper Executable (Linux/macOS)**
   ```bash
   chmod +x ./gradlew
   ```

3. **Build Release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   
   Or build Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Locate the Generated APK**
   - Release: `app/build/outputs/apk/release/app-release.apk`
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`

5. **Install on Connected Device**
   ```bash
   ./gradlew installDebug
   ```

### Method 3: Building Signed Release APK

For production distribution, create a signed APK:

1. **Open Project in Android Studio**

2. **Generate Signing Key**
   - Go to `Build` → `Generate Signed Bundle / APK`
   - Select `APK` and click `Next`
   - Click `Create new` to generate a new keystore
   - Fill in the details:
     - **Key store path**: Choose location (e.g., `call2cloud.jks`)
     - **Password**: Create a strong password
     - **Key alias**: `call2cloud`
     - **Key password**: Create a strong password
   - Click `Next`

3. **Select Build Variant**
   - Choose `release` variant
   - Click `Next`

4. **Finish**
   - Click `Finish` to generate the signed APK
   - Location: `app/release/app-release.apk`

**Important**: Backup your keystore file (`call2cloud.jks`) securely. You'll need it for future updates!

## Build Configuration Details

The current build configuration supports:

- **Minimum API Level**: 29 (Android 10)
- **Target API Level**: 35 (Android 15)
- **Supported Architectures**: ARM64 (primary), ARMv7 (fallback)
- **Java Version**: 17
- **Kotlin Compiler Extension**: 1.5.11

## Included Dependencies

- **Compose UI Framework**: Modern declarative UI
- **Room Database**: Local data storage
- **WorkManager**: Background task scheduling
- **Google Auth & Drive APIs**: Cloud integration
- **Media3 ExoPlayer**: Audio playback support

## Troubleshooting

### Build Fails - "SDK not found"
- Open Android Studio SDK Manager: `Tools` → `SDK Manager`
- Install API 35, build-tools 35.x, and NDK

### Gradle Sync Issues
- Close Android Studio
- Delete `.gradle` and `.idea` folders in the android directory
- Reopen Android Studio and wait for sync

### Memory Issues During Build
- Increase heap size in `gradle.properties`:
  ```
  org.gradle.jvmargs=-Xmx4096m
  ```

### APK Installation Fails on Device
- Ensure device has Android 10 or higher
- Check USB Debugging is enabled
- Uninstall previous version: `adb uninstall com.call2cloud.app`
- Try installing via: `adb install app-debug.apk`

## APK Testing

After building, test the APK:

1. **Install on Device**
   ```bash
   adb install app-release.apk
   ```

2. **Launch Application**
   - Open app drawer and find "Call2Cloud"
   - Grant necessary permissions when prompted

3. **Verify Features**
   - Test call functionality
   - Check cloud integration
   - Verify audio playback

## Security Considerations

- Use signed release APKs for distribution
- Never commit keystore files to version control
- Store keystore password securely
- Test on multiple Android versions (10, 12, 14, 15)

## Distribution

Once built and tested, you can distribute the APK via:
- **Google Play Store**: Upload to Play Console
- **Direct APK Distribution**: Share file directly
- **GitHub Releases**: Attach APK to release notes

For Play Store submission:
- Use the signed release APK
- Follow Google Play Store guidelines
- Submit for review and testing

---

**Questions or Issues?** Check the `APK_BUILD_ALTERNATIVES.md` for additional build methods.
