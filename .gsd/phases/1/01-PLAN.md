---
phase: 1
plan: 1
wave: 1
depends_on: []
files_modified:
  - build.gradle (app)
  - settings.gradle
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/pdascanner/urovo/LoginActivity.java
  - app/src/main/res/layout/activity_login.xml
  - app/src/main/res/values/colors.xml
  - app/src/main/res/values/strings.xml
  - app/src/main/res/values/dimens.xml
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Android project builds with ./gradlew assembleDebug (0 errors)"
    - "LoginActivity displays username and password fields on launch"
    - "Any non-empty username + password grants access to DashboardActivity"
    - "Empty username OR empty password shows a visible error message (Toast or TextInputLayout error)"
    - "Login button has a minimum 48dp touch target"
  artifacts:
    - "app/src/main/java/com/pdascanner/urovo/LoginActivity.java exists"
    - "app/src/main/res/layout/activity_login.xml exists"
    - "AndroidManifest.xml declares LoginActivity as LAUNCHER activity"
---

# Plan 1.1: Android Project Scaffold + LoginActivity

<objective>
Create the Android project skeleton and implement the dummy login screen.

Purpose: Everything else depends on a buildable project and a working entry point.
Output: Buildable Gradle project + LoginActivity with dummy auth that navigates to DashboardActivity placeholder.
</objective>

<context>
Load for context:
- .gsd/SPEC.md
- .gsd/phases/1/RESEARCH.md
</context>

<tasks>

<task type="auto">
  <name>Create Android project scaffold with correct minSdk/targetSdk and warehouse color theme</name>
  <files>
    build.gradle (root),
    app/build.gradle,
    settings.gradle,
    app/src/main/AndroidManifest.xml,
    app/src/main/res/values/colors.xml,
    app/src/main/res/values/strings.xml,
    app/src/main/res/values/dimens.xml,
    app/src/main/res/values/themes.xml
  </files>
  <action>
    1. Create a standard Android project (Java, no Jetpack Compose) at the repo root:
       - Package: com.pdascanner.urovo
       - minSdkVersion 26, targetSdkVersion 29, compileSdkVersion 34
       - applicationId "com.pdascanner.urovo"

    2. In app/build.gradle add Apache POI dependency block (needed in Phase 2 but configure now to avoid dependency conflicts later):
       ```groovy
       implementation('org.apache.poi:poi-ooxml:5.2.3') {
           exclude group: 'org.apache.xmlbeans', module: 'xmlbeans'
       }
       implementation 'org.apache.xmlbeans:xmlbeans:5.1.1'
       implementation 'com.fasterxml.jackson.core:jackson-core:2.14.2'
       ```
       Also add in android {} block:
       ```groovy
       packagingOptions {
           exclude 'META-INF/DEPENDENCIES'
           exclude 'META-INF/LICENSE'
           exclude 'META-INF/NOTICE'
           exclude 'META-INF/LICENSE.txt'
       }
       ```

    3. In colors.xml define the warehouse high-contrast palette:
       - colorPrimary: #1A1A2E (deep navy)
       - colorPrimaryVariant: #16213E
       - colorAccent / colorSecondary: #F5A623 (high-vis amber)
       - colorBackground: #0F3460
       - colorSurface: #1A1A2E
       - colorOnPrimary: #FFFFFF
       - colorError: #FF4444
       - colorTextPrimary: #FFFFFF
       - colorTextSecondary: #B0BEC5

    4. In dimens.xml: button_height = 56dp, button_text_size = 18sp, scan_count_text_size = 14sp

    5. AndroidManifest.xml:
       - Declare LoginActivity as LAUNCHER / MAIN intent-filter
       - Declare DashboardActivity (no intent-filter)
       - Add permission: android.permission.WRITE_EXTERNAL_STORAGE (maxSdkVersion="28")
       - Add permission: android.permission.READ_EXTERNAL_STORAGE (maxSdkVersion="28")

    AVOID: Do NOT use Jetpack Compose or ViewBinding — use classic XML layouts + findViewById for compatibility with the DT50s Android 10 environment.
    AVOID: Do NOT set targetSdk higher than 29 — the DT50s runs Android 10 and the ScanManager API is tested against it.
  </action>
  <verify>Run: ./gradlew assembleDebug — must complete with BUILD SUCCESSFUL and 0 errors.</verify>
  <done>Project compiles. AndroidManifest has both activities declared. colors.xml has all 9 color tokens.</done>
</task>

<task type="auto">
  <name>Implement LoginActivity with dummy auth and high-contrast glove-friendly UI</name>
  <files>
    app/src/main/java/com/pdascanner/urovo/LoginActivity.java,
    app/src/main/res/layout/activity_login.xml
  </files>
  <action>
    Layout (activity_login.xml):
    - Root: ConstraintLayout, background = @color/colorBackground
    - App title TextView: "PDA Scanner", large bold, centered, colorAccent text
    - Username TextInputLayout + TextInputEditText (inputType=text), hint="Username", white text on dark card
    - Password TextInputLayout + TextInputEditText (inputType=textPassword), hint="Password"
    - LOGIN button: height=@dimen/button_height, background=colorAccent, bold white text, full width
    - All touch targets ≥ 48dp. No small labels or crowded inputs.

    LoginActivity.java logic:
    ```
    onClick(LOGIN button):
      username = usernameField.getText().toString().trim()
      password = passwordField.getText().toString().trim()

      if (username.isEmpty()) {
          usernameLayout.setError("Username is required")
          return
      }
      if (password.isEmpty()) {
          passwordLayout.setError("Password is required")
          return
      }
      // Dummy auth — any non-empty values pass
      usernameLayout.setError(null)
      passwordLayout.setError(null)
      Intent intent = new Intent(this, DashboardActivity.class)
      startActivity(intent)
      finish()  // prevent back-navigation to login while logged in
    ```

    Create DashboardActivity.java as an empty stub (just extends AppCompatActivity, setContentView a blank layout) so the project compiles.

    AVOID: Do NOT use SharedPreferences to "remember" the username — session must be stateless.
    AVOID: Do NOT call finish() before startActivity() — finish() after ensures back stack is cleared correctly.
  </action>
  <verify>
    1. ./gradlew assembleDebug — BUILD SUCCESSFUL
    2. Install on device/emulator: adb install app/build/outputs/apk/debug/app-debug.apk
    3. Launch app — login screen appears with username + password fields and LOGIN button.
    4. Tap LOGIN with empty fields — error messages appear on both fields.
    5. Enter any username + password, tap LOGIN — navigates to DashboardActivity (blank screen).
    6. Press back — does NOT go back to login (finish() prevents it).
  </verify>
  <done>
    - Empty fields → error messages shown, navigation blocked.
    - Non-empty fields → navigates to DashboardActivity, back press stays on Dashboard.
    - UI is dark navy background with amber LOGIN button, large touch targets.
  </done>
</task>

</tasks>

<verification>
After all tasks:
- [ ] ./gradlew assembleDebug succeeds with 0 errors
- [ ] LoginActivity is the app launcher screen
- [ ] Empty credential validation works (both fields checked independently)
- [ ] Successful login navigates to DashboardActivity and removes login from back stack
- [ ] High-contrast color scheme visible on screen
</verification>

<success_criteria>
- [ ] All tasks verified per <verify> steps
- [ ] Must-haves confirmed above
</success_criteria>
