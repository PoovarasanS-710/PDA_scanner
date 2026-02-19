# SPEC.md — Project Specification

> **Status**: `FINALIZED`
> **Date**: 2026-02-19
> **Project**: PDA Scanner Mobile App — Urovo DT50s

---

## Vision

A minimal, robust native Android app for the Urovo DT50s handheld terminal that enables warehouse and field operators to scan barcodes/QR codes using the device's integrated scanner, view scans in real-time, export session data as Excel, and guarantee complete data clearance on logout — all without any backend, cloud, or complex onboarding.

---

## Goals

1. **Core scanning workflow** — Integrate directly with the Urovo DT50s scanner SDK; capture barcodes and QR codes with a single tap, no manual entry.
2. **Real-time dashboard** — Display every valid scan instantly in a scrollable, timestamped list (newest first) for in-session verification.
3. **One-tap Excel export** — Generate and save a `.xlsx` file of all scanned session data on demand.
4. **Secure session management** — Dummy login/logout cycle that guarantees zero data persistence across sessions.

---

## Non-Goals (Out of Scope)

- No cloud sync, backend integration, or network connectivity of any kind.
- No inventory management, item lookup, or ERP/WMS integration.
- No multi-user roles, permissions, or collaboration features.
- No onboarding wizard or multi-step tutorial.
- No data persistence across sessions (by design).

---

## Users

**Primary persona: Warehouse Worker / Field Operator**
- Uses Urovo DT50s handheld terminal in a warehouse or field environment.
- May wear gloves; works under variable lighting; prioritises speed and accuracy over features.
- Shares the device between shifts; expects a clean slate on each login.
- Needs simple, high-contrast UI with large touch targets.

---

## Constraints

### Technical
- **Platform**: Native Android (targeting Urovo DT50s hardware).
- **Scanner SDK**: Must use Urovo DT50s proprietary scanner SDK/API — no camera-based scanning.
- **No backend**: Pure local, single-session, in-memory data storage.
- **Excel export**: Uses an Android-compatible `.xlsx` generation library (e.g., Apache POI or equivalent).
- **Session capacity**: Must handle ≥ 500 scans per session without performance degradation.
- **Response time**: UI must reflect each scan in < 500 ms.

### Timeline
- **Total**: 1–2 weeks.
- **Phase 1** (Core): ~1 week — Auth, scanning, dashboard, session management.
- **Phase 2** (Export & Polish): ~0.5 week — Excel export, UX refinement, QA.

### Team
- 1 Mobile Developer (Android + Urovo SDK).
- 1 optional Product Lead / QA.

---

## Success Criteria

- [ ] User can log in with any non-empty username and password (dummy auth).
- [ ] Tapping "Scan" activates the Urovo DT50s hardware scanner.
- [ ] Every successful scan appears at the top of the dashboard list with its data string and timestamp within 500 ms.
- [ ] Duplicate scans are rejected — user receives feedback (vibration or toast).
- [ ] Invalid / empty scan events produce an error message or vibration; no bad data is added to the list.
- [ ] "Export to Excel" generates a valid `.xlsx` file containing all session scans.
- [ ] Export button is disabled / shows warning when the scan list is empty.
- [ ] Logging out clears all session scan data and returns to the login screen.
- [ ] A fresh login shows an empty scan list.
- [ ] App sustains ≥ 500 scans per session with < 500 ms UI update per scan.
- [ ] App crash-free rate > 99% under normal use.
- [ ] Scanner hardware errors surface a clear modal message rather than a silent failure.
