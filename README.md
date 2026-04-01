# soen345-project

SOEN 345 team project: Android ticket reservation app for browsing events and booking tickets.

## Notification Configuration

Add these entries to `local.properties` at project root:

```properties
VONAGE_API_KEY=your_vonage_api_key
VONAGE_API_SECRET=your_vonage_api_secret
VONAGE_FROM=your_vonage_sender_number
MAILGUN_API_KEY=your_mailgun_api_key
MAILGUN_DOMAIN=your_mailgun_domain
MAILGUN_FROM_EMAIL=Your App <postmaster@your_mailgun_domain>
```

These values are read into `BuildConfig` fields at build time.
