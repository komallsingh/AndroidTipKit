# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Maven publishing **dry-run** configuration. The four library modules (`nudgekit-core`, `nudgekit-datastore`, `nudgekit-compose`, `nudgekit-compose-datastore`) apply `maven-publish` with shared coordinates `io.github.abdullajon1881:<module>:0.2.0-alpha.1` (group/version centralized in `gradle.properties`), sources JARs, and full POM metadata (name, description, URL, Apache-2.0 license, developer, SCM). `./gradlew publishToMavenLocal` generates artifacts into `~/.m2`. The `sample` app is intentionally not published.
- Publishing groupId is `io.github.abdullajon1881` (Maven Central auto-verifies it against the GitHub account, no domain needed). The Kotlin package / Android namespace remain `dev.nudgekit.*` and are intentionally unchanged.
- GPG signing scaffolding (`signing` plugin) on all four library modules using in-memory PGP keys. Signing is **gated**: it only activates when `signingInMemoryKey` (+ optional `signingInMemoryKeyId` / `signingInMemoryKeyPassword`) is supplied via `-P` or `ORG_GRADLE_PROJECT_*` env vars. With no keys present, signing is skipped so builds and CI stay green. No keyring is stored on disk and no key is committed. Signed artifacts are still a dry-run target — nothing is uploaded to a remote repository.
- **Not** configured: GPG signing and Maven Central / Sonatype upload — both intentionally deferred to later phases. Nothing is published to any remote repository.

## [0.2.0-alpha.1] - 2026-05-28

Second alpha. Tooling, project hygiene, a Compose module split, and
SDK-agnostic analytics hooks. **Still not published to Maven Central** — use
the local modules (see the README). Public APIs are additive and
source-compatible with `0.1.0-alpha.1`; the only change for managed-component
users is an added Gradle dependency (see "Migration" below).

### Added
- SDK-agnostic analytics hooks. New `TipAnalytics` interface and `NoOpTipAnalytics` object in `nudgekit-core` (`onTipShown`, `onTipDismissed`, `onTipActionClicked`, all with default no-op bodies). No analytics SDK is bundled and no networking is added — consumers forward events to Firebase / Mixpanel / a logger themselves.
- `ManagedInlineTip` and `ManagedTipBox` gained an optional `analytics: TipAnalytics = NoOpTipAnalytics` parameter (appended last, so existing callers are unaffected). `onTipShown` fires in lock-step with `markShown` (once per appearance), `onTipDismissed` on dismiss, and `onTipActionClicked` on the action button. Existing `onActionClick` behaviour is preserved.
- Sample app shows an "Analytics Events" section backed by a small `SampleTipAnalytics`, appending strings like `shown: use_filters` / `action: enable_notifications`.
- New `nudgekit-compose-datastore` module that houses the state-aware managed components (`ManagedInlineTip`, `ManagedTipBox`). It depends on `nudgekit-core`, `nudgekit-datastore`, and `nudgekit-compose`, and is the future home for managed-component UI tests.
- Initial Compose UI test coverage for `nudgekit-compose` using Robolectric (local JVM, no emulator): 11 tests for the pure-UI components `InlineTip` (title/message rendering, action-button visibility, dismiss/action callbacks) and `TipBox` (wrapped content, visible/hidden states, Top position).
- `nudgekit-core` tests for `TipAnalytics`: `NoOpTipAnalytics` is a no-throwing `TipAnalytics`, default interface bodies allow partial overrides, and a recording fake captures events in order (5 tests).
- GitHub Actions CI (`.github/workflows/ci.yml`): on push to `main` and PRs, runs on `ubuntu-latest` with Temurin JDK 17 + Gradle caching, executing `:nudgekit-core:test`, `:nudgekit-datastore:test`, `:nudgekit-compose:test`, building `:nudgekit-compose-datastore`, assembling the sample debug APK, and the full `build`. README shows a CI status badge.
- Community / contributor files: issue templates (bug report, feature request), pull request template, `CONTRIBUTING.md`, `SECURITY.md`, and `CODE_OF_CONDUCT.md`.
- `.gitattributes` for cross-platform line-ending normalization.

### Changed
- **`nudgekit-compose` is now pure UI.** It no longer depends on `nudgekit-datastore`; consumers who only want `InlineTip` / `TipBox` no longer pull in DataStore transitively. `ManagedInlineTip` and `ManagedTipBox` moved to the new `nudgekit-compose-datastore` module. Both Compose modules share the Kotlin package `dev.nudgekit.compose`, so existing imports (e.g. `dev.nudgekit.compose.ManagedInlineTip`) are unchanged — only the Gradle dependency differs.
- The sample app now depends on `nudgekit-compose-datastore` for the managed components.
- `nudgekit-compose` disables the release unit-test variant; Compose UI tests run against the debug variant only (the `ui-test-manifest` test Activity is debug-only).

### Migration from 0.1.0-alpha.1
- If you use the managed components (`ManagedInlineTip` / `ManagedTipBox`), add the new module dependency: `implementation(project(":nudgekit-compose-datastore"))`. Imports are unchanged — they remain in package `dev.nudgekit.compose`.
- Pure-UI-only consumers can keep depending on just `nudgekit-compose`, which no longer drags in DataStore.

### Tests
- Project total: **126 tests, 0 failures** — 78 in `nudgekit-core`, 37 in `nudgekit-datastore`, 11 in `nudgekit-compose`. Verified on JDK 17.

### Known Limitations
- Not published to Maven Central yet — use the local modules (see the README).
- Managed-component UI tests (`ManagedInlineTip` / `ManagedTipBox`) are still deferred; they need a deterministic DataStore + Compose test harness. Pure-UI components are covered, and the managed components are exercised by the sample app.
- `TipBox` positioning is intentionally simple (Start/End use a fixed `widthIn(max = 240.dp)`), not pixel-perfect.
- Managed components observe all counters via `observeCounters()` and use `collectAsState` instead of `collectAsStateWithLifecycle`.
- Maven publishing is not configured.

## [0.1.0-alpha.1] - 2026-05-25

### Added
- Initial `nudgekit-core` alpha with the core tip model, rule system, evaluator, state model, counters, and manager contract.
- Built-in rules for dismissal, single display, max display count, event thresholds, screen visit thresholds, minimum interval, and custom predicates.
- Android-free `nudgekit-core` module so eligibility logic can stay independent from Android framework code.
- Initial `nudgekit-datastore` alpha with `DataStoreTipManager` persistence, evaluation helpers, and reactive observation APIs.
- Initial `nudgekit-compose` alpha with `InlineTip`, `ManagedInlineTip`, `TipBox`, `ManagedTipBox`, `TipPosition`, and default styling helpers.
- Sample app demonstrating inline tips, anchored tips, event tracking, screen tracking, and reset flows.
- Initial project documentation covering setup, rules, datastore usage, Compose UI, sample app behavior, limitations, and roadmap.

### Known Limitations
- No Compose UI tests yet.
- `TipBox` positioning is intentionally simple and not pixel-perfect.
- `nudgekit-compose` currently depends on `nudgekit-datastore`.
- Managed components currently observe all counters and use `collectAsState` instead of `collectAsStateWithLifecycle`.
- Maven publishing is not configured yet.
