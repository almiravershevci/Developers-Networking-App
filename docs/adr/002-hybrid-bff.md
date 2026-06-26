# ADR 002: Hybrid REST BFF

## Status
Accepted

## Context
Dashboard analytics and admin broadcast need aggregated reads/writes not ideal for mobile SDK alone.

## Decision
Add a **Node Express BFF** with Firebase JWT auth, OpenAPI, and authorization mirroring Firestore rules. Mount **`/api/v1`** with legacy **`/api`** alias for Android compatibility.

## Consequences
- Extra deploy surface (optional Render/Railway)
- Clear API contract for tooling and tests
- Must keep BFF authz in sync with rules
