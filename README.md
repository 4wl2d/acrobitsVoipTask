# Acrobits VoIP Task

Kotlin Android prototype for the Acrobits interview assignment. The app registers
one SIP account with Acrobits LibSoftphone SDK and places outgoing calls to
another SIP account, typically logged in on CloudSoftphone on a second device.

The public repository intentionally does not contain the assigned license key,
SIP usernames, or SIP password. Use the values from the assignment locally via
ignored Gradle properties or environment variables.

## What Is Implemented

- Welcome screen that requests microphone permission before showing the dialer.
- Dialer screen with SIP registration status, SIP username/password inputs,
  phone-number input validation, and a `Dial` button that is enabled only after
  successful SIP registration and valid number input.
- Outgoing call screen with remote display name/number, call state, call
  duration, `Mute`, `Hold`, and `Hang Up` controls.
- Acrobits LibSoftphone SDK integration for SDK initialization, account
  provisioning, registration, outbound calls, mute, hold, and hangup.
- Unit tests for core validation and calling state behavior.
- Compose instrumentation tests that exercise screen clicks and route/ViewModel
  wiring from registration through call controls.
- Architecture guard task that checks important module boundaries.

## Key Decisions

- The app is split into small modules instead of keeping SDK, UI, and state
  logic in one Android app module. This keeps the assignment easy to review and
  demonstrates separation of concerns without adding a full Clean Architecture
  use-case layer for a single flow.
- Acrobits SDK code is isolated in `:softphone:acrobits`. UI and ViewModel code
  depend only on the `SoftphoneClient` interface from `:core:voip`, so the
  feature can be tested without loading the SDK.
- Runtime secrets are local configuration, not source code. The license key and
  SIP credentials are passed through `local.properties`,
  `~/.gradle/gradle.properties`, `-P...` properties, or environment variables.
- The SDK version is pinned to `25.3.17`, the version recommended in the task,
  instead of using `latest`, so builds remain reproducible.
- SIP account setup follows the assignment guidance: SaaS provisioning license,
  PBX host `pbx.acrobits.cz`, standard ICM mode, and `tls+sip:` transport to
  avoid common SIP ALG issues.
- The project uses Kotlin, AndroidX, Jetpack Compose, coroutines, JUnit, and
  Acrobits LibSoftphone. No extra third-party product libraries are used beyond
  the SDK allowed by the assignment.
- Git history was organized through feature branches merged into `develop`, then
  `develop` merged into `main`, so the default branch contains the final
  submission state.

## Architecture

The project is organized by responsibility:

- `:app` is the composition root. It owns the Android application, launch
  activity, `BuildConfig` values, and dependency wiring.
- `:feature:calling` owns the calling flow: permission gate, route composable,
  ViewModel, immutable UI state, validation display, and Compose screens.
- `:softphone:acrobits` implements `SoftphoneClient` using Acrobits
  LibSoftphone and hides SDK globals/listeners behind the core interface.
- `:core:voip` is a pure Kotlin module with stable contracts and models:
  `SoftphoneClient`, `VoipConfig`, `SipCredentials`, registration state,
  call-session state, phone-number validation, and clock abstraction.
- `:core:designsystem` contains the shared Compose Material 3 theme.

Dependency direction:

```text
:app -> :feature:calling
:app -> :softphone:acrobits
:app -> :core:designsystem
:app -> :core:voip

:feature:calling -> :core:designsystem
:feature:calling -> :core:voip

:softphone:acrobits -> :core:voip
```

Boundary rules:

- Only `:softphone:acrobits` imports `cz.acrobits.*`.
- Only `:app` reads generated `BuildConfig` values.
- `:feature:calling` receives `SoftphoneClient` and `VoipConfig` as inputs.
- SDK-specific permissions stay with the SDK adapter; microphone permission
  requested by the user flow stays with the calling feature.
- Pure validation and state models stay in `:core:voip` for fast unit tests.

See `docs/architecture.md` for the dedicated architecture note.

## Requirements

- Android Studio
- JDK 17 or newer
- Android SDK installed locally
- Android device or emulator for connected Compose tests
- A second device with CloudSoftphone for the real SIP call smoke test
- Assignment-provided Acrobits Maven/license values and SIP credentials

## Local Configuration

Copy `local.properties.example` to ignored `local.properties`, or put the same
keys in `~/.gradle/gradle.properties`. Do not commit real values.

```properties
sdk.dir=/path/to/android/sdk

acrobitsMavenUsername=net.acrobits.interview.test.android
acrobitsMavenPassword=<assignment-license-key>
acrobitsLicenseKey=<assignment-license-key>
```

If Acrobits issued a different Maven username for your account, override
`acrobitsMavenUsername` locally. The application ID and license target used by
this project are:

```text
net.acrobits.interview.test.android
```

Optional local SIP form prefill:

```properties
defaultSipUsername=<assigned-sip-username>
defaultSipPassword=<assigned-sip-password>
```

Equivalent environment variables:

```text
ACROBITS_MAVEN_USERNAME
ACROBITS_MAVEN_PASSWORD
ACROBITS_LICENSE_KEY
DEFAULT_SIP_USERNAME
DEFAULT_SIP_PASSWORD
```

## Build

Run a full build:

```bash
./gradlew build --no-daemon
```

Install the debug app on a connected device:

```bash
./gradlew :app:installDebug --no-daemon
```

Launch it from adb:

```bash
adb shell am start -n net.acrobits.interview.test.android/com.tomilov.acrobitsvoip.MainActivity
```

## Verification

Run architecture and JVM/unit checks:

```bash
./gradlew checkArchitecture :core:voip:test :feature:calling:testDebugUnitTest --no-daemon
```

Run the Compose click and route integration tests on a connected Android device
or emulator:

```bash
adb devices
adb shell input keyevent KEYCODE_WAKEUP
adb shell am force-stop net.acrobits.interview.test.android
./gradlew :feature:calling:connectedDebugAndroidTest --no-daemon --rerun-tasks
```

If a physical device was used for manual testing before connected tests, unlock
it first. Some OEM devices leave the manually launched app in a state where
Compose tests cannot find a hierarchy unless the app is force-stopped.

```bash
adb shell input keyevent KEYCODE_WAKEUP
adb shell input swipe 500 1800 500 500 300
adb shell am force-stop net.acrobits.interview.test.android
./gradlew :feature:calling:connectedDebugAndroidTest --no-daemon --rerun-tasks
```

Before submitting or pushing, clean generated build outputs that may contain
local `BuildConfig` values:

```bash
./gradlew clean --no-daemon
```

## Manual SIP Test Flow

Use one assigned SIP account in this app and the other assigned SIP account in
CloudSoftphone on a second device.

1. Install CloudSoftphone on the second device.
2. Log into CloudSoftphone with the other assigned account using the Cloud ID
   format from the assignment, for example `<assigned-username>@INTERVIEW`, and
   the assigned password.
3. Install and launch this app.
4. Grant microphone permission on the welcome screen.
5. Enter one assigned SIP username/password in the dialer, unless local prefill
   was configured.
6. Tap `Register`.
7. Confirm the registration status label becomes `Registered`.
8. Enter the other assigned extension in the phone-number field.
9. Tap `Dial`.
10. Confirm CloudSoftphone receives the call.
11. Answer the call and verify audio.
12. Verify `Mute`, `Hold`, and `Hang Up` on the outgoing call screen.

Expected result: registration succeeds, the outgoing call reaches
CloudSoftphone, call duration advances after the call is established, and hangup
returns the app to the dialer.

## Test Coverage

- `:core:voip:test` covers pure validation/domain behavior.
- `:feature:calling:testDebugUnitTest` covers ViewModel behavior with a fake
  `SoftphoneClient`, including registration and the guard that prevents dialing
  before registration.
- `:feature:calling:connectedDebugAndroidTest` covers Compose UI clicks for the
  welcome, dialer, and call screens, plus route/ViewModel wiring with a fake
  softphone client.
- `checkArchitecture` enforces source-level boundaries so Acrobits SDK imports
  do not leak into feature/app UI code and `BuildConfig` usage stays in `:app`.

## Submission Safety

Before publishing, verify the working tree does not contain assignment secrets:

```bash
git status --short
git grep -n -E '<real-sip-password>|<real-license-key>' -- ':!local.properties'
```

The actual assignment SIP usernames and password should stay in the assignment
document, ignored local configuration, or manual app input. They should not be
committed to the public repository.

## Prototype Scope

This is an outgoing-call prototype. Incoming calls are expected to be received by
CloudSoftphone on a second device for assignment testing. The app includes the
outgoing call controls required by the task, but it does not add a full incoming
call UX, production notification flow, account management, or persistence beyond
the SDK account configuration needed for registration.
