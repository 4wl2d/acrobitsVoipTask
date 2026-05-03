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

Acrobits Maven and runtime license values are intentionally not committed.
Configure them in ignored `local.properties`, `~/.gradle/gradle.properties`,
`-P...` command-line properties, or environment variables:

```properties
acrobitsMavenUsername=net.acrobits.interview.test.android
acrobitsMavenPassword=<license-key>
acrobitsLicenseKey=<license-key>
```

`local.properties.example` contains a copyable template without secrets.

SIP account credentials are entered in the app at runtime. For local QA, ignored
Gradle properties can prefill the account form:

```properties
defaultSipUsername=<sip-username>
defaultSipPassword=<sip-password>
```

Equivalent environment variables are `ACROBITS_MAVEN_USERNAME`,
`ACROBITS_MAVEN_PASSWORD`, `ACROBITS_LICENSE_KEY`, `DEFAULT_SIP_USERNAME`, and
`DEFAULT_SIP_PASSWORD`.

## Manual Test Flow

1. Install the app and grant microphone permission on the welcome screen.
2. Enter one assigned SIP account in the app and tap `Register`.
3. Wait until the registration indicator reads `Registered`.
4. Log into CloudSoftphone on another device with the other assigned SIP account.
5. Enter the other extension in the dialer and tap `Dial`.
