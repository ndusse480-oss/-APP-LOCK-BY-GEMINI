# Samsung One UI Dark Theme App Lock - Complete Android Project

This is the complete, native Android Studio project in Kotlin for the Samsung One UI Minimalist Dark App Lock.

## Architecture Highlights
1. **Background Foreground Monitoring (`AppLockService.kt`)**: Uses `UsageStatsManager` to detect when a protected app enters foreground.
2. **Reboot Autostart (`BootReceiver.kt`)**: Listens to `BOOT_COMPLETED` so it resumes protection immediately when your Samsung phone restarts.
3. **Biometrics Integration (`LockOverlayActivity.kt`)**: Directly invokes Android's `BiometricPrompt` for Samsung Ultrasonic/Optical fingerprint sensors and Face recognition.
4. **Samsung AMOLED Black Aesthetic**: Pure `#000000` dark theme tailored for One UI.

---

## 3 Ways to Convert into an APK:

### Method 1: Using Android Studio (Recommended if you have a PC/Mac)
1. Download and extract this project folder.
2. Open **Android Studio** and click **Open**, then select this extracted folder.
3. Allow Gradle to sync dependencies.
4. Go to top menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Once complete, click **locate** to find `app-debug.apk`.
6. Transfer `app-debug.apk` to your Samsung phone (via Quick Share, USB, or Google Drive) and install it!

### Method 2: Free Cloud Build via GitHub Actions (No Android Studio or PC needed!)
1. Create a free account on [GitHub.com](https://github.com).
2. Create a new private or public repository (e.g. `samsung-app-lock`).
3. Upload all the files from this zip into your repository.
4. The included `.github/workflows/build_apk.yml` file will automatically run!
5. In your GitHub repo, click the **Actions** tab, tap the latest run, and under **Artifacts**, download `SamsungOneUIAppLock-debug.zip`.
6. Unzip it to get your ready-to-install `app-debug.apk` file!

### Method 3: Direct phone build via Termux or AIDE
- You can compile Gradle directly on Android using Termux or AIDE app from Play Store.

---

## Samsung One UI Setup After Installation:
1. **Grant Permissions**:
   - Open App Lock.
   - Tap to grant **Usage Access** (so it knows when WhatsApp/Gallery is opened).
   - Tap to grant **Appear on top** (so it draws the lock screen).
2. **Prevent Samsung Battery Sleep on Restart**:
   - Go to **Settings → Apps → One UI App Lock → Battery → Unrestricted**.
   - Go to **Device Care → Battery → Background usage limits → Never sleeping apps → Add App Lock**.
