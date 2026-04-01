# soen345-project

SOEN 345 team project: Android ticket reservation app for browsing events and booking tickets.

## SMS Confirmation

Add these entries to `local.properties` at project root:

```properties
VONAGE_API_KEY=your_vonage_api_key
VONAGE_API_SECRET=your_vonage_api_secret
VONAGE_FROM=YourSenderId
```

These values are read into `BuildConfig` fields at build time.
