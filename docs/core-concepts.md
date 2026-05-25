# Core Concepts

NudgeKit is built around a small set of types in `nudgekit-core`. This module is pure Kotlin — **no Android dependency** — so it builds and tests on the JVM in milliseconds.

```
┌──────────────────────────────────────────────────────────┐
│  nudgekit-core  (pure Kotlin, no Android)                │
│                                                          │
│   Tip ──── rules ──── TipRule (sealed)                   │
│    │                                                     │
│    │       evaluated by                                  │
│    └──────────────────► TipEvaluator                     │
│                              │                           │
│                              ├─ reads TipState           │
│                              ├─ reads TipCounters        │
│                              └─ returns TipDecision      │
│                                                          │
│   TipManager (interface)                                 │
└──────────────────────────────────────────────────────────┘
        ▲                                       ▲
        │ implements                            │ uses
        │                                       │
┌─────────────────┐                  ┌────────────────────┐
│ DataStoreTip-   │                  │ ManagedInlineTip   │
│ Manager         │                  │ ManagedTipBox      │
│ (nudgekit-      │                  │ (nudgekit-compose) │
│  datastore)     │                  └────────────────────┘
└─────────────────┘
```

## `Tip`

An immutable value object. Define tips as top-level `val` constants and reuse the same instance everywhere.

```kotlin
data class Tip(
    val id: String,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val priority: Int = 0,
    val rules: List<TipRule> = listOf(TipRule.NotDismissed),
)
```

- `id` is the persistence key. Choose a stable, descriptive string.
- All string fields must be non-blank (`require` runs in `init`).
- `priority` is currently informational. It will be used by tip-group features in a later release.

## `TipRule`

A sealed interface describing eligibility. Rules are evaluated in declaration order; the first one that fails short-circuits evaluation.

Built-in rules: `NotDismissed`, `Once`, `MaxDisplayCount`, `AfterEvent`, `AfterScreenVisits`, `MinIntervalHours`, `Custom`. See [rules.md](rules.md) for examples of each.

## `TipState`

Per-tip state persisted by the manager:

```kotlin
data class TipState(
    val tipId: String,
    val isDismissed: Boolean = false,
    val displayCount: Int = 0,
    val lastShownAtMillis: Long? = null,
)
```

You normally do not construct `TipState` yourself — the manager produces it. Default values represent a tip that has never been shown or dismissed.

## `TipCounters`

Global counters across all tips:

```kotlin
data class TipCounters(
    val eventCounts: Map<String, Int> = emptyMap(),
    val screenVisitCounts: Map<String, Int> = emptyMap(),
) {
    fun eventCount(name: String): Int        // 0 if missing
    fun screenVisitCount(name: String): Int  // 0 if missing
}
```

Counters are bumped by `trackEvent(name)` and `trackScreen(screenName)`.

## `TipContext`

A read-only bundle passed into rule evaluation:

```kotlin
data class TipContext(
    val tip: Tip,
    val state: TipState,
    val counters: TipCounters,
    val nowMillis: Long,
)
```

Useful for `TipRule.Custom` predicates, which receive `TipContext` as a receiver.

## `TipDecision`

The result of evaluation:

```kotlin
sealed interface TipDecision {
    data object Show : TipDecision
    data class Hide(val reason: TipHideReason) : TipDecision
}
```

`TipHideReason` carries diagnostic data — useful for logging, analytics, or debug UIs:

```kotlin
sealed interface TipHideReason {
    data object Dismissed : TipHideReason
    data object AlreadyShownOnce : TipHideReason
    data object MaxDisplayCountReached : TipHideReason
    data class  EventCountNotReached(val eventName: String, val required: Int, val actual: Int) : TipHideReason
    data class  ScreenVisitCountNotReached(val screenName: String, val required: Int, val actual: Int) : TipHideReason
    data class  MinIntervalNotReached(val requiredHours: Int, val elapsedMillis: Long) : TipHideReason
    data object CustomRuleFailed : TipHideReason
}
```

## `TipEvaluator`

A simple class with one job: take `(tip, state, counters, now)` and return a `TipDecision`. Time is injectable so tests are deterministic:

```kotlin
class TipEvaluator {
    suspend fun evaluate(
        tip: Tip,
        state: TipState,
        counters: TipCounters,
        nowMillis: Long = System.currentTimeMillis(),
    ): TipDecision

    suspend fun shouldShow(...): Boolean
}
```

Pure function (apart from clock). 100% Android-free.

## `TipManager`

The write-side interface:

```kotlin
interface TipManager {
    suspend fun trackEvent(name: String)
    suspend fun trackScreen(screenName: String)
    suspend fun dismiss(tipId: String)
    suspend fun markShown(tipId: String)
    suspend fun reset(tipId: String)
    suspend fun resetAll()
}
```

A concrete implementation lives in [`nudgekit-datastore`](datastore.md). The interface itself stays Android-free.

## Managed UI

`nudgekit-compose` provides `ManagedInlineTip` and `ManagedTipBox`. They glue everything together:

1. Subscribe to `observeTipState` and `observeCounters`.
2. Call `shouldShow` to decide visibility.
3. Call `markShown` exactly once per appearance.
4. Call `dismiss` when the user taps the close button.

You don't have to use the managed components — `InlineTip` and `TipBox` are pure UI and work with any visibility source. See [compose-ui.md](compose-ui.md).
