---
phase: 1
plan: 4
wave: 2
depends_on: ["01", "02", "03"]
files_modified:
  - app/src/main/java/com/pdascanner/urovo/DashboardActivity.java
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Tapping LOGOUT shows a confirmation dialog with the current scan count"
    - "Confirming logout calls ScanSession.clearAll() and navigates to LoginActivity"
    - "After logout, pressing back does NOT return to DashboardActivity"
    - "Logging in again shows an empty scan list (0 scans)"
    - "Cancelling the logout dialog keeps the user on Dashboard with list intact"
  artifacts:
    - "DashboardActivity contains performLogout() that calls scanSession.clearAll() then navigates with FLAG_ACTIVITY_CLEAR_TASK"
---

# Plan 1.4: Logout Flow + Session Clear Verification

<objective>
Verify and harden the logout flow: confirmation dialog → ScanSession.clearAll() → return to LoginActivity with cleared back stack.

Purpose: This is a top-priority requirement (REQ-15, REQ-16). Plan 1.3 includes the logout stub — this plan verifies it's wired correctly and adds the edge case: cancel should not log out.
Output: Verified, production-ready logout flow. No code changes expected if Plan 1.3 implemented correctly — this is a verification + hardening plan.
</objective>

<context>
Load for context:
- .gsd/SPEC.md (REQ-15, REQ-16, REQ-17)
- app/src/main/java/com/pdascanner/urovo/DashboardActivity.java
- app/src/main/java/com/pdascanner/urovo/ScanSession.java
</context>

<tasks>

<task type="auto">
  <name>Harden logout in DashboardActivity — verify FLAG_ACTIVITY_CLEAR_TASK and scanSession.clearAll()</name>
  <files>app/src/main/java/com/pdascanner/urovo/DashboardActivity.java</files>
  <action>
    Review the performLogout() method implemented in Plan 1.3. Ensure it:
    1. Calls `scanSession.clearAll()` BEFORE starting LoginActivity.
    2. Uses `Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK` — this removes DashboardActivity from the back stack entirely.
    3. Does NOT call `finish()` explicitly (FLAG_ACTIVITY_CLEAR_TASK handles it).

    If these conditions are met, no code change needed — mark as verified.
    If any condition is missing, fix it now.

    Also verify the confirmLogout() AlertDialog:
    - Positive button: "Logout" → calls performLogout()
    - Negative button: "Cancel" → dialog dismisses only, NO logout, NO clearAll()

    AVOID: Do NOT use FLAG_ACTIVITY_NEW_TASK alone — it doesn't clear the back stack.
    AVOID: Do NOT clear session data inside the dialog builder lambda for the Cancel path.
  </action>
  <verify>
    1. Scan 3 barcodes on Dashboard (count = 3).
    2. Tap LOGOUT → AlertDialog appears: "You have 3 scan(s). Logging out will clear all data. Continue?"
    3. Tap Cancel → dialog closes, Dashboard still shows 3 scans. ✅
    4. Tap LOGOUT again → tap Logout → LoginActivity appears, back button does NOT go to Dashboard. ✅
    5. Login again → Dashboard shows 0 scans. ✅
  </verify>
  <done>
    - Logout dialog shows correct scan count.
    - Cancel leaves session unchanged.
    - Confirm clears session and returns to login with cleared back stack.
    - Post-logout login shows empty list.
  </done>
</task>

<task type="checkpoint:human-verify">
  <name>End-to-end session integrity test on Urovo DT50s</name>
  <action>
    Manual verification checklist on physical device:
    1. Fresh install (or clear app data): launch app → Login screen appears.
    2. Login with "test" / "1234" → Dashboard shows 0 scans, EXPORT disabled.
    3. Scan 5 distinct barcodes → all 5 appear in list, newest first, with timestamps.
    4. Scan one of the same barcodes again → Toast "Duplicate scan ignored", count stays 5.
    5. Force-kill the app (swipe away from recents) → reopen → Login screen, Dashboard shows 0 scans. ✅ (REQ-17)
    6. Scan 2 barcodes again → Tap LOGOUT → dialog shows "2 scan(s)".
    7. Cancel → still on Dashboard with 2 scans.
    8. Logout → LoginActivity, back does NOT work.
    9. Log in again → 0 scans. ✅
  </action>
  <verify>All 9 steps pass on physical Urovo DT50s.</verify>
  <done>Phase 1 end-to-end flow verified on hardware.</done>
</task>

</tasks>

<verification>
After all tasks:
- [ ] Logout dialog shows current scan count
- [ ] Cancel preserves session data
- [ ] Confirm clears session and clears back stack
- [ ] Post-logout login shows empty dashboard
- [ ] Force-kill → relaunch → empty dashboard (REQ-17)
</verification>

<success_criteria>
- [ ] All tasks verified per <verify> steps
- [ ] Must-haves confirmed above
- [ ] Physical device end-to-end test completed by user
</success_criteria>
