# Acrobits VoIP Task

Android project scaffold for the Acrobits VoIP task.

## Requirements

- Android Studio
- JDK 17 or newer

## Build

```bash
./gradlew build
```

## SDK Configuration

The app is configured for the Acrobits assignment application ID
`net.acrobits.interview.test.android` and LibSoftphone `25.3.17`.

Acrobits Maven and runtime license values can be overridden without changing
source:

```properties
acrobitsMavenUsername=net.acrobits.interview.test.android
acrobitsMavenPassword=<license-key>
acrobitsLicenseKey=<license-key>
```

SIP account credentials are entered in the app at runtime. For local QA, ignored
`local.properties` values can prefill the account form:

```properties
defaultSipUsername=<sip-username>
defaultSipPassword=<sip-password>
```

## Manual Test Flow

1. Install the app and grant microphone permission on the welcome screen.
2. Enter one assigned SIP account in the app and tap `Register`.
3. Wait until the registration indicator reads `Registered`.
4. Log into CloudSoftphone on another device with the other assigned SIP account.
5. Enter the other extension in the dialer and tap `Dial`.
