# DECISIONS.md — Architecture Decision Records

> **Project**: PDA Scanner — Urovo DT50s

---

## ADR Template

```
### ADR-NNN: [Title]
**Date**: YYYY-MM-DD
**Status**: Proposed | Accepted | Deprecated
**Context**: Why is this decision needed?
**Decision**: What was decided?
**Consequences**: What are the trade-offs?
```

---

## Decisions

### ADR-001: Native Android (No Cross-Platform Framework)
**Date**: 2026-02-19
**Status**: Accepted
**Context**: The app must integrate with Urovo DT50s proprietary scanner SDK. Cross-platform frameworks (Flutter, React Native) do not provide reliable native SDK bridge support for industrial PDA hardware.
**Decision**: Build as a native Android app using Java or Kotlin.
**Consequences**: Faster scanner integration; requires developer with Android expertise; no iOS port possible from this codebase.

---

### ADR-002: In-Memory Session Storage Only
**Date**: 2026-02-19
**Status**: Accepted
**Context**: PRD requires zero data persistence between sessions for privacy and compliance. Writing to SharedPreferences or a local DB would risk data surviving a crash or reboot.
**Decision**: All scan data stored in a ViewModel / singleton scoped to the application process lifetime. No writes to disk or database during a session.
**Consequences**: Data is automatically purged on app process death (force-stop, reboot) — meets REQ-17. No recovery after crash — acceptable per PRD.

---

### ADR-003: Apache POI (or Equivalent) for Excel Export
**Date**: 2026-02-19
**Status**: Proposed
**Context**: Need `.xlsx` file generation on Android without network access.
**Decision**: Evaluate Apache POI (Java) or a lightweight Android `.xlsx` library (e.g., `xlsx-lite`, `sjXLSX`). Choose based on APK size impact and Android compatibility.
**Consequences**: Adds a library dependency; must verify compatibility with target Android version on DT50s.
