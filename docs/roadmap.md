# Roadmap

Planned work, in roughly the order we expect to tackle it. Subject to change based on feedback.

## v0.1 — MVP (current)

- [x] `nudgekit-core`: `Tip`, `TipRule` (7 variants), `TipState`, `TipCounters`, `TipContext`, `TipDecision`, `TipHideReason`, `TipEvaluator`, `TipManager`.
- [x] `nudgekit-datastore`: `DataStoreTipManager` with snapshot reads, reactive flows, evaluation helpers.
- [x] `nudgekit-compose`: `InlineTip`, `TipBox`, `ManagedInlineTip`, `ManagedTipBox`, `TipPosition`, `NudgeTipDefaults`, `NudgeTipColors`, previews.
- [x] Working sample app exercising all of the above.
- [x] 110 passing unit tests across `nudgekit-core` and `nudgekit-datastore`.

## v0.2 — Hardening and publishing

- [ ] Maven Central publishing (`maven-publish` plugin, signing config, staging workflow).
- [ ] CI pipeline (GitHub Actions): build, test, lint, sample APK assembly.
- [x] Split `nudgekit-compose` so pure-UI consumers don't pull in DataStore. Managed components moved to a new `nudgekit-compose-datastore` module; `nudgekit-compose` no longer depends on `nudgekit-datastore`.
- [ ] Compose UI tests for the managed variants (`ManagedInlineTip`, `ManagedTipBox`) in `nudgekit-compose-datastore`. (Pure-UI tests for `InlineTip` / `TipBox` already exist.)
- [ ] Adopt `collectAsStateWithLifecycle` as opt-in via a parameter or separate artifact.
- [ ] Accessibility audit of all UI components (touch targets, content descriptions, dynamic type, dark mode contrast).

## v0.3 — UX and ergonomics

- [ ] Real popover physics for `TipBox` Start/End positions (proper positioning, arrow, edge clamping).
- [ ] Animation customization on `InlineTip` (caller-supplied `EnterTransition` / `ExitTransition`).
- [ ] Tip groups / mutual exclusion ("only show one tip from this group at a time").
- [ ] `priority` field becomes meaningful — used to pick the highest-priority eligible tip within a group.
- [ ] `TipRule.ExpiresAt(timestampMillis)` and `TipRule.ExpiresAfter(durationMillis)` for time-bounded tips.
- [x] Optional `TipAnalytics` interface so consumers can observe `onTipShown`, `onTipDismissed`, `onTipActionClicked`. SDK-agnostic, no bundled dependency; wired into the managed components.

## v0.4 — KMP and broader reach

- [ ] Migrate `nudgekit-core` to a Kotlin Multiplatform module (`commonMain`) so the rule engine is reusable on iOS, desktop, and JVM backends.
- [ ] Keep `nudgekit-datastore` Android-only; provide a `MemoryTipManager` in core for testing and KMP use.
- [ ] iOS UI components (SwiftUI + a Compose Multiplatform variant) — exploratory.

## v0.5+ — Tooling and ecosystem

- [ ] In-app debug overlay showing eligibility status for every tip on screen (with `TipHideReason`).
- [ ] Lint rules / IDE inspections for common mistakes (duplicate tip IDs, unreachable rules).
- [ ] Documentation site (Dokka + a small static site).
- [ ] Recipe gallery (paywall nudges, premium upsell, A/B-testable copy variants).
- [ ] Optional Room-backed `TipManager` for apps that already store user state in Room.

## Out of scope (for now)

These are explicitly **not** planned:

- Remote configuration / server-driven tip definitions. Apps can fetch their own `Tip` objects from a backend if they want this.
- Built-in analytics integrations (Firebase, Amplitude, etc). The `TipAnalytics` hook is the integration point.
- Network calls inside the library. NudgeKit stays offline and dependency-light.
- Replacing `TooltipBox`. NudgeKit and `TooltipBox` solve different problems (see [README](../README.md#why-not-just-tooltipbox)).

## How to influence the roadmap

Open an issue describing your use case. Real-world tip patterns are the best forcing function for new built-in rules and components. Bug reports with reproduction steps are especially welcome while the library is pre-1.0.
