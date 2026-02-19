# REQUIREMENTS.md — Testable Requirements

> **Project**: PDA Scanner — Urovo DT50s
> **Derived from**: SPEC.md (FINALIZED)

---

| ID | Requirement | Source | Priority | Status |
|----|-------------|--------|----------|--------|
| REQ-01 | App displays a login screen with username and password fields on launch | SPEC Goal 4 | High | Pending |
| REQ-02 | Any non-empty username + password combination grants access (dummy auth) | SPEC Goal 4 | High | Pending |
| REQ-03 | Empty username or password is rejected with a visible error message | SPEC Goal 4 | High | Pending |
| REQ-04 | Tapping "Scan" activates the Urovo DT50s hardware scanner via SDK/API | SPEC Goal 1 | High | Pending |
| REQ-05 | A successful scan appends the data string and timestamp to the top of the list | SPEC Goal 2 | High | Pending |
| REQ-06 | UI reflects each new scan within 500 ms of capture | SPEC Constraint | High | Pending |
| REQ-07 | Duplicate scan values (same string) within a session are rejected silently or with feedback | SPEC Success Criteria | High | Pending |
| REQ-08 | Empty or invalid scan events produce an error toast / vibration; no entry is added | SPEC Goal 1 | High | Pending |
| REQ-09 | Scanner hardware errors surface a modal dialog with a clear error message | SPEC Success Criteria | High | Pending |
| REQ-10 | Dashboard list is scrollable and shows all scans in newest-first order | SPEC Goal 2 | High | Pending |
| REQ-11 | Each list item shows: scanned data string + formatted timestamp | SPEC Goal 2 | High | Pending |
| REQ-12 | "Export to Excel" generates a valid `.xlsx` file with all session scans | SPEC Goal 3 | Medium | Pending |
| REQ-13 | Exported file is saved to an accessible folder; user sees file path or success confirmation | SPEC Goal 3 | Medium | Pending |
| REQ-14 | "Export to Excel" button is disabled (or shows a warning) when the scan list is empty | SPEC Goal 3 | Medium | Pending |
| REQ-15 | Tapping "Logout" clears all in-memory session scan data and returns to login screen | SPEC Goal 4 | High | Pending |
| REQ-16 | A fresh login after logout shows an empty scan list | SPEC Goal 4 | High | Pending |
| REQ-17 | If the app crashes or device reboots, no scan data persists on restart | SPEC Goal 4 | High | Pending |
| REQ-18 | App sustains ≥ 500 scans per session without UI lag | SPEC Constraint | High | Pending |
| REQ-19 | All interactive buttons use large touch targets suitable for gloved hands | SPEC Users | Medium | Pending |
| REQ-20 | UI uses a high-contrast colour palette legible under warehouse lighting | SPEC Users | Medium | Pending |
| REQ-21 | Rapid consecutive scans are debounced to prevent duplicate hardware events | SPEC Edge Cases | Medium | Pending |
