---
phase: 2
plan: 1
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/pdascanner/urovo/ExportManager.java
  - app/src/main/res/layout/activity_dashboard.xml
  - app/src/main/res/layout/item_scan.xml
  - app/src/main/res/values/themes.xml
  - app/src/main/res/values/dimens.xml
autonomous: true
user_setup: []

must_haves:
  truths:
    - "ExportManager.exportToXlsx() generates .xlsx with columns: #, Barcode, Timestamp"
    - "Export runs on background thread (ExecutorService) — UI never blocks"
    - "Callback onSuccess delivers file path on main thread; onError delivers error message"
    - "Export button disabled + Toast when scan list is empty"
    - "Export success shows AlertDialog with file path"
    - "Export failure shows AlertDialog with error message"
    - "All buttons are ≥ 48dp touch target"
    - "High-contrast palette applied (navy background, amber accents)"
    - "ScanItem list items have ≥ 56dp minHeight for glove-friendly tapping"
  artifacts:
    - "app/src/main/java/com/pdascanner/urovo/ExportManager.java exists"
---

# Plan 2.1: Excel Export + UI Polish

<objective>
Wire ExportManager into DashboardActivity, apply warehouse-grade UI polish, verify all REQ-12–REQ-14 and REQ-19–REQ-20.

Purpose: Phase 2 finalises the export flow and ensures the glove-friendly, high-contrast UI is production-ready.
Output: Working xlsx export, fully polished UI, self-QA against all REQs.
</objective>

<tasks>

<task type="auto">
  <name>Verify ExportManager integration in DashboardActivity</name>
  <files>app/src/main/java/com/pdascanner/urovo/DashboardActivity.java</files>
  <action>
    Confirm DashboardActivity.exportToExcel() correctly:
    1. Guards for empty list (Toast + return when count == 0)
    2. Calls ExportManager.exportToXlsx(context, items, callback)
    3. onSuccess: shows AlertDialog with file path
    4. onError: shows AlertDialog with error message

    No changes expected if already correct — mark verified.
  </action>
  <verify>Code review of exportToExcel() method confirms all 4 conditions.</verify>
  <done>Export wired correctly with empty guard and success/error dialogs.</done>
</task>

<task type="auto">
  <name>UI Polish — verify glove-friendly touch targets and high-contrast palette</name>
  <files>
    app/src/main/res/layout/activity_dashboard.xml,
    app/src/main/res/layout/item_scan.xml,
    app/src/main/res/values/dimens.xml,
    app/src/main/res/values/colors.xml
  </files>
  <action>
    Verify all of the following are already in place:
    - All buttons in activity_dashboard.xml use android:layout_height="@dimen/button_height" (56dp)
    - item_scan.xml CardView minHeight ≥ 56dp
    - colors.xml has navy/amber/red high-contrast tokens
    - SCAN button backgroundTint = colorButtonScan (amber)
    - LOGOUT button backgroundTint = colorButtonLogout (red)
    - EXPORT button alpha set correctly (0.4 when disabled)

    Fix any that are missing; no changes expected if already correct.
  </action>
  <verify>All visual checks pass — layouts use dimension tokens, colors match spec.</verify>
  <done>Glove-friendly touch targets and high-contrast palette confirmed.</done>
</task>

<task type="auto">
  <name>Self-QA — verify all REQ-01 through REQ-21 in codebase</name>
  <files>all source files</files>
  <action>
    Systematic code review against all requirements.
    Run ./gradlew assembleDebug as the final gate.
  </action>
  <verify>./gradlew assembleDebug — BUILD SUCCESSFUL. 0 errors.</verify>
  <done>All requirements addressed. APK ready for device install.</done>
</task>

</tasks>

<verification>
- [ ] ExportManager.java exists with XSSFWorkbook implementation
- [ ] exportToXlsx runs on background thread (ExecutorService)
- [ ] Callback on main thread (Handler(mainLooper))
- [ ] Empty guard in DashboardActivity.exportToExcel()
- [ ] Success AlertDialog with file path
- [ ] Error AlertDialog with error message
- [ ] All buttons ≥ 48dp
- [ ] ./gradlew assembleDebug → BUILD SUCCESSFUL
</verification>
