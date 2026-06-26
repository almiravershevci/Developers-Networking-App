# Firestore composite indexes

Documented rationale for `firestore.indexes.json`.

| Index | Query |
|-------|-------|
| projects: visibility + lifecycle + updatedAt | Public recruiting board sorted by recency |
| projects: stackTags + lifecycle + createdAt | Stack filter on discover |
| projects: ownerUserId + createdAt | Owner dashboard |
| matchRequests: to/from + workflow + createdAt | Pending invites lists |
| conversations: participantIds + lastMessageAt | Chat inbox ordering |
| inbox: recipientUserId + createdAt | Notification feed pagination |
| events: startsAt + status | Calendar views |
| tasks (collection group): boardColumn + updatedAt | Cross-project task queries |
| collaboratorSuggestions: viewer + rank | Dashboard suggestions |
| newsHighlights: sortOrder + publishedAt | Curated news strip |

Deploy: `firebase deploy --only firestore:indexes`
