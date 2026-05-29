# Limitations

NudgeKit is an MVP. This document is the honest list of what does not work yet, and the trade-offs you should know about before adopting it.

## Distribution

- **Not published to Maven Central.** Coordinates like `io.github.abdullajon1881:nudgekit-core:0.3.0-alpha.1` are placeholders. Use the modules locally for now (Git submodule + `includeBuild`, or vendored sources).
- **No version tags.** No semantic-versioning guarantees yet. APIs may change before the first published release.

## Testing

- **Compose UI tests** (Robolectric, local JVM) cover both the pure-UI components (`InlineTip`, `TipBox`) and the managed components (`ManagedInlineTip`, `ManagedTipBox`).
- **No KMP test target.** `:nudgekit-core` is pure Kotlin/JVM today and tested as such. It is not yet a Kotlin Multiplatform module.

Test counts:

- `:nudgekit-core` — 78 unit tests, 0 failures.
- `:nudgekit-datastore` — 44 unit tests, 0 failures.
- `:nudgekit-compose` — 11 Compose UI tests (Robolectric), 0 failures.
- `:nudgekit-compose-datastore` — 6 Compose UI tests (Robolectric), 0 failures.
- Total: **139 tests, 0 failures**.

## Module structure

- **Managed components live in `nudgekit-compose-datastore`.** `nudgekit-compose` is now pure UI and depends only on `nudgekit-core` — it no longer pulls in DataStore. The state-aware `ManagedInlineTip` and `ManagedTipBox` moved to the new `nudgekit-compose-datastore` module (which depends on `nudgekit-core`, `nudgekit-datastore`, and `nudgekit-compose`). Both Compose modules share the Kotlin package `dev.nudgekit.compose`, so consumer imports are unchanged.

## Compose components

- **Broad counter observation.** `ManagedInlineTip` and `ManagedTipBox` subscribe to `observeCounters()` and re-evaluate whenever *any* counter changes — not only the ones referenced by the tip's rules. Fine for typical apps; suboptimal at scale.
- **Sticky-show (a deliberate design choice, not a bug).** Managed components treat "currently showing" as sticky local state: once a tip is on screen it stays for that appearance and is only re-evaluated when hidden or dismissed. This stops a tip from flickering away mid-view if `markShown` mutates state that a rule depends on. A `MaxDisplayCount(n)` tip therefore shows **exactly `n` times**, and the persisted `displayCount` reaches exactly `n` (the evaluator hides at `displayCount >= n`).
- **`TipBox` Start/End** lay the tip beside the anchor in a `Row`. The tip takes up to half the width, capped at 240 dp, and respects layout direction (RTL). It is in-flow, not a floating popover (overlay-style anchoring with an arrow is on the roadmap).
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
