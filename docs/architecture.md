# Architecture

The project is split by responsibility instead of by technical layer names alone. The app is still small, but module boundaries make the dependencies visible and keep the Acrobits SDK isolated from the UI.

## Modules

- `:app` is the composition root. It owns the Android application, launch activity, `BuildConfig` values, and dependency wiring.
- `:feature:calling` owns the calling user flow: microphone permission gate, route composable, ViewModel, immutable UI state, and Compose screens.
- `:softphone:acrobits` is the infrastructure adapter for Acrobits LibSoftphone. It implements `SoftphoneClient` and hides SDK listener/global APIs behind a factory that returns the core interface.
- `:core:voip` is a pure Kotlin module for the stable VoIP contract and domain models: `SoftphoneClient`, `VoipConfig`, call/registration models, SIP credentials, phone-number validation, and clock abstraction.
- `:core:designsystem` owns the shared Compose Material 3 theme used by the app and previews.

## Dependency Direction

```text
:app
  -> :feature:calling
  -> :softphone:acrobits
  -> :core:designsystem
  -> :core:voip

:feature:calling
  -> :core:designsystem
  -> :core:voip

:softphone:acrobits
  -> :core:voip

:core:designsystem
  -> Compose Material 3

:core:voip
  -> Kotlin/coroutines only
```

The important rule is that dependencies point inward toward stable contracts. The UI depends on `SoftphoneClient`, not Acrobits. The app builds the concrete Acrobits client only at the edge.

## Boundary Rules

- Only `:softphone:acrobits` imports `cz.acrobits.*` classes.
- Only `:app` reads generated `BuildConfig` values and turns local Gradle secrets into runtime configuration.
- `:feature:calling` receives `SoftphoneClient` and `VoipConfig` as inputs, so the feature can be tested or previewed without the SDK.
- SDK-related permissions live in `:softphone:acrobits`; the microphone permission requested by the UI lives in `:feature:calling`; the app manifest only owns the launcher/application declaration.
- Pure validation and state models live in `:core:voip`, which keeps unit tests fast and independent of Android.
