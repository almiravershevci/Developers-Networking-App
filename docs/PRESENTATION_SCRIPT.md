# Presentation Script (~30 seconds)

Use this for a demo intro, viva, or submission video voice-over.

---

> We use **MVVM** with **one repository microservice per feature**.  
> **Firebase Auth** handles identity — including email, username, and Google sign-in.  
> **Firestore** is the system of record, protected by **collection-level security rules**.  
> User-generated data — chat, tasks, and profiles — is written by clients.  
> Curated and aggregate data — events, stats, and inbox notifications — is **server-written** via the Admin SDK, seed scripts, and planned Cloud Functions.  
> We integrate the **Hacker News REST API** for tech trends on Search, and **local notifications** (with FCM planned) for task reminders and admin push.

---

## One-line version

MVVM + Firebase Auth + Firestore rules; client writes for chat/tasks/profiles; server writes for events/stats/inbox; Hacker News API + push notifications.
