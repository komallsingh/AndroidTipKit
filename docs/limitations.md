# Limitations

NudgeKit is an MVP. This document is the honest list of what does not work yet, and the trade-offs you should know about before adopting it.

## Distribution

- **Not published to Maven Central.** Coordinates like `dev.nudgekit:nudgekit-core:0.1.0` are placeholders. Use the modules locally for now (Git submodule + `includeBuild`, or vendored sources).
- **No version tags.** No semantic-versioning guarantees yet. APIs may change before the first published release.

## Testing

- **Compose UI tests cover the pure-UI components only.** `:nudgekit-compose` now has Robolectric-based Compose tests for `InlineTip` and `TipBox` (rendering, action/dismiss buttons, visibility). The **managed** components (`ManagedInlineTip`, `ManagedTipBox`) are not yet covered by automated tests — they are still verified by previews and the sample app. Adding managed-component coverage (DataStore + Compose `mainClock` coordination) is the next testing priority.
- **No KMP test target.** `:nudgekit-core` is pure Kotlin/JVM today and tested as such. It is not yet a Kotlin Multiplatform module.

Test counts:

- `:nudgekit-core` — 78 unit tests, 0 failures.
- `:nudgekit-datastore` — 37 unit tests, 0 failures.
- `:nudgekit-compose` — 11 Compose UI tests (Robolectric), 0 failures.
- Total: **126 tests, 0 failures**.

## Module structure

- **Managed components live in `nudgekit-compose-datastore`.** `nudgekit-compose` is now pure UI and depends only on `nudgekit-core` — it no longer pulls in DataStore. The state-aware `ManagedInlineTip` and `ManagedTipBox` moved to the new `nudgekit-compose-datastore` module (which depends on `nudgekit-core`, `nudgekit-datastore`, and `nudgekit-compose`). Both Compose modules share the Kotlin package `dev.nudgekit.compose`, so consumer imports are unchanged.

## Compose components

- **`collectAsState` not `collectAsStateWithLifecycle`.** Managed components do not pause Flow collection when the host lifecycle is in the background. Adding `androidx.lifecycle:lifecycle-runtime-compose` is on the roadmap so consumers can opt in.
- **Broad counter observation.** `ManagedInlineTip` and `ManagedTipBox` subscribe to `observeCounters()` and re-evaluate whenever *any* counter changes — not only the ones referenced by the tip's rules. Fine for typical apps; suboptimal at scale.
- **`MaxDisplayCount` sticky-show.** Without intervention, the Nth display would briefly appear and then disappear because `markShown` pushes `displayCount` past the limit. Managed components avoid this by treating "currently showing" as a sticky local state and re-evaluating only when the tip is hidden or externally dismissed. This is correct UX but means the **persisted display count can reach `count + 1`** on the Nth show. If you read `displayCount` directly, treat `>= count` as "max reached."
- **`TipBox` Start/End positions** use a fixed `widthIn(max = 240.dp)` inside a `Row`. Intentionally simple for now; may not look right on very narrow screens or with very long messages. Real popover-style positioning is on the roadmap.
- **Dismiss button touch target.** The dismiss `IconButton` is 40 dp, slightly below the Material 48 dp recommendation, chosen to fit small cards. We will revisit this once we have accessibility-focused UI tests.

## DataStore

- **Single Preferences file.** All tip state and counters live in one file (`nudgekit_preferences`). Suitable for typical app usage (dozens of tips, modest event tracking) but not designed for thousands of entries.
- **No migration support.** If the key schema changes between releases, you will have to handle the migration manually or call `resetAll()`. The schema is documented in [datastore.md](datastore.md) for stability.
- **No TTL / auto-expiry.** Persisted state lives until the user uninstalls or you call `reset` / `resetAll`. An `ExpiresAfter` rule could be added later.
- **DataStore singleton.** Creating two `DataStoreTipManager` instances pointing at the same file is officially undefined behavior. The sample uses an Activity-scoped instance for simplicity, but production apps should use one process-wide instance.

## Core API

- **`priority` field is informational.** `Tip.priority` exists for future tip-group / mutual-exclusion features but is not used by the current evaluator.
- **No OR combinator.** Rules are AND-ed together. For OR semantics, use `TipRule.Custom { … || … }`.
- **`TipRule.Custom` predicate equality is undefined.** It is intentionally not a `data class`. Comparing two `Custom` rules with `==` is meaningless.
- **No analytics hook.** There is no `TipAnalytics` interface yet. Wrap `markShown` / `dismiss` calls in your own code if you want to log events.

## Build / tooling

- **JDK 17 required.** The toolchain pins JDK 17 explicitly. JDK 21+ is not yet validated.
- **No CI.** This repo does not yet have a CI configuration. `./gradlew build` is the recommended local gate.
- **No publishing config.** Adding `maven-publish` and signing is part of the pre-release work.

See [roadmap.md](roadmap.md) for what we plan to address and in roughly what order.
