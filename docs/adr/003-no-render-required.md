# ADR 003: Render optional — Firestore-only teammates

## Status
Accepted

## Context
Not all teammates can run or pay for hosted Node API during development.

## Decision
Ship with **`DevConnectApiConfig.ENABLED = false`**. Mobile uses Firestore directly; BFF is optional for lead/admin tooling.

## Consequences
- Zero local Node requirement for most developers
- BFF features (REST analytics) require explicit enable + deploy
- Documentation must state two valid modes
