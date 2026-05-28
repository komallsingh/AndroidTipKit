# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- New `nudgekit-compose-datastore` module that houses the state-aware managed components (`ManagedInlineTip`, `ManagedTipBox`). It depends on `nudgekit-core`, `nudgekit-datastore`, and `nudgekit-compose`, and is the future home for managed-component UI tests.
- Initial Compose UI test coverage for `nudgekit-compose` using Robolectric (local JVM, no emulator): 11 tests for the pure-UI components `InlineTip` (title/message rendering, action-button visibility, dismiss/action callbacks) and `TipBox` (wrapped content, visible/hidden states, Top position). Managed components remain TODO.
- CI now runs `./gradlew :nudgekit-compose:test` and builds `:nudgekit-compose-datastore`.

### Changed
- **`nudgekit-compose` is now pure UI.** It no longer depends on `nudgekit-datastore`; consumers who only want `InlineTip` / `TipBox` no longer pull in DataStore transitively. `ManagedInlineTip` and `ManagedTipBox` moved to the new `nudgekit-compose-datastore` module. Both Compose modules share the Kotlin package `dev.nudgekit.compose`, so existing imports (e.g. `dev.nudgekit.compose.ManagedInlineTip`) are unchanged — only the Gradle dependency differs.
- The sample app now depends on `nudgekit-compose-datastore` for the managed components.
- `nudgekit-compose` disables the release unit-test variant; Compose UI tests run against the debug variant only (the `ui-test-manifest` test Activity is debug-only).

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
