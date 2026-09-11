# Security & Privacy Audit: Zero Third-Party Guarantee
1. Permissions: Only READ_PHONE_STATE, RECORD_AUDIO, FOREGROUND_SERVICE, POST_NOTIFICATIONS, INTERNET.
2. No hidden servers, no analytics, no contacts collection, no accessibility abuse.
3. Destination: User's personal Google Drive exclusively (Scope: drive.file).
4. Audio Format: Opus mono 32-48 kbps with AAC fallback.
5. Verification: MD5 cryptographic hash match before local delete prompt.