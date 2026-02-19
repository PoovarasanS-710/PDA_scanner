# ROADMAP.md — Phase Structure

> **Project**: PDA Scanner — Urovo DT50s
> **Current Phase**: Not started
> **Milestone**: v1.0 — Production-ready scan session app
> **Timeline**: 1–2 weeks

---

## Must-Haves (from SPEC)

- [ ] Dummy login / logout with full session data clearance (REQ-01 – REQ-03, REQ-15 – REQ-17)
- [ ] Urovo DT50s hardware scanner integration (REQ-04 – REQ-09, REQ-21)
- [ ] Real-time scrollable dashboard with timestamped scan list (REQ-10, REQ-11)
- [ ] In-memory session storage only; zero cross-session persistence (REQ-17)
- [ ] Excel `.xlsx` export with empty-list guard (REQ-12 – REQ-14)
- [ ] High-contrast, glove-friendly UI (REQ-19, REQ-20)

---

## Phases

### Phase 1: Foundation — Auth, Scanner & Dashboard
**Status**: ✅ Complete
**Objective**: Deliver a working app skeleton — login/logout, live scanner integration, and the scrollable scan list — with all data clearing on logout.
**Estimated Duration**: ~1 week
**Requirements**: REQ-01, REQ-02, REQ-03, REQ-04, REQ-05, REQ-06, REQ-07, REQ-08, REQ-09, REQ-10, REQ-11, REQ-15, REQ-16, REQ-17, REQ-18, REQ-21

**Deliverables:**
- [ ] Android project scaffolding (package name, minSdk, target device config)
- [ ] `LoginActivity` — username/password fields, dummy validation, error state
- [ ] Urovo DT50s scanner SDK integrated (`ScannerManager`, broadcast receiver or callback)
- [ ] `DashboardActivity` — RecyclerView list, newest-first ordering, timestamp formatting
- [ ] `ScanSession` singleton/ViewModel — in-memory list, duplicate detection, debounce logic
- [ ] Logout action — clears session data, navigates back to `LoginActivity`
- [ ] Hardware scanner error handling — modal dialog on SDK error/disconnect
- [ ] Session data guaranteed non-persistent (no SharedPreferences/DB writes)

---

### Phase 2: Export & Polish
**Status**: ⬜ Not Started
**Objective**: Add Excel export, apply warehouse-grade UX polish, and perform self-QA against all requirements.
**Estimated Duration**: ~0.5 week
**Requirements**: REQ-12, REQ-13, REQ-14, REQ-19, REQ-20

**Deliverables:**
- [ ] Apache POI (or equivalent) integrated for `.xlsx` file generation
- [ ] `ExportManager` — builds workbook (columns: #, Scanned Data, Timestamp), saves to Downloads or app-specific folder
- [ ] Export success dialog showing file path; error dialog on write failure
- [ ] "Export to Excel" button disabled + toast when scan list is empty
- [ ] UI pass: large touch targets (min 48 dp), high-contrast palette, accessible font sizes
- [ ] Orientation/layout check on DT50s screen dimensions
- [ ] Self-QA checklist pass against all REQ-01 through REQ-21
- [ ] Manual end-to-end test on physical Urovo DT50s hardware

---

### Phase 3: Hardening & Release
**Status**: ⬜ Not Started
**Objective**: Stress test, edge-case coverage, finalise APK for distribution.
**Estimated Duration**: Buffer / optional
**Requirements**: REQ-06, REQ-08, REQ-17, REQ-18

**Deliverables:**
- [ ] 500-scan stress test — verify no lag or memory issues
- [ ] Crash / app-kill session integrity test (verify no data persists after force-stop)
- [ ] Scanner rapid-fire debounce test
- [ ] Signed release APK or internal distribution package
- [ ] `CHANGELOG.md` — v1.0 notes
