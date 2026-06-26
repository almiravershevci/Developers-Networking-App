# ADR 001: Firestore-first data layer

## Status
Accepted

## Context
Team capstone with mobile-first delivery and shared Firebase project.

## Decision
Use **Cloud Firestore** as the single source of truth. Android reads/writes via SDK; server automation via Cloud Functions; optional Node BFF for analytics/admin.

## Consequences
- Real-time UX without custom WebSocket infra
- Security depends on strong rules + minimal Admin SDK usage
- Complex queries require composite indexes
