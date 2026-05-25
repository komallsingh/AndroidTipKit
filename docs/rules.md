# Rules

A `Tip` becomes eligible to be shown when **every** rule in its `rules` list passes. Rules are evaluated in declaration order and short-circuit on the first failure — that means cheaper, more selective rules should come first.

```kotlin
val tip = Tip(
    id = "example",
    title = "Title",
    message = "Body",
    rules = listOf(
        TipRule.NotDismissed,                          // cheap, common
        TipRule.AfterScreenVisits("checkout", 2),      // counter lookup
        TipRule.MaxDisplayCount(3),                    // state check
    ),
)
```

## `NotDismissed`

Passes while the tip has not been dismissed.

```kotlin
rules = listOf(TipRule.NotDismissed)
```

Almost every real tip should include this. It is the default if you don't pass a `rules` list at all.

## `Once`

Passes only when the tip has never been shown (`displayCount == 0`).

```kotlin
val welcome = Tip(
    id = "welcome",
    title = "Welcome",
    message = "Thanks for installing.",
    rules = listOf(TipRule.Once),
)
```

Use for one-shot, first-run experiences.

## `MaxDisplayCount(count)`

Passes while `displayCount < count`. After being shown `count` times, the tip stops appearing.

```kotlin
rules = listOf(
    TipRule.NotDismissed,
    TipRule.MaxDisplayCount(3),  // show up to 3 times
)
```

`count` must be positive (`> 0`) — enforced at construction.

> **Note on the managed components:** `ManagedInlineTip` and `ManagedTipBox` use a sticky-show model. Once a tip becomes visible, it stays visible until dismissed even if `markShown` would otherwise push state past `MaxDisplayCount`. This prevents a one-frame flicker on the Nth show. See [limitations.md](limitations.md).

## `AfterEvent(eventName, count)`

Passes once `manager.trackEvent(eventName)` has been called at least `count` times.

```kotlin
val tip = Tip(
    id = "save_search",
    title = "Save Your Search",
    message = "We can alert you when matches appear.",
    rules = listOf(
        TipRule.NotDismissed,
        TipRule.AfterEvent("search_run", 5),
    ),
)

// Anywhere in your app:
manager.trackEvent("search_run")
```

`eventName` must be non-blank, `count` must be positive.

## `AfterScreenVisits(screenName, count)`

Passes once `manager.trackScreen(screenName)` has been called at least `count` times.

```kotlin
val tip = Tip(
    id = "save_address",
    title = "Save Your Address",
    message = "Skip address entry next time.",
    rules = listOf(
        TipRule.NotDismissed,
        TipRule.AfterScreenVisits("checkout", 2),
    ),
)

// On the checkout screen:
LaunchedEffect(Unit) { manager.trackScreen("checkout") }
```

## `MinIntervalHours(hours)`

Passes when at least `hours` hours have elapsed since `lastShownAtMillis`. If the tip has never been shown, the rule passes immediately.

```kotlin
val tip = Tip(
    id = "weekly_reminder",
    title = "Try Premium",
    message = "One week free.",
    rules = listOf(
        TipRule.NotDismissed,
        TipRule.MinIntervalHours(24 * 7), // once a week
    ),
)
```

Useful for periodic nudges that should not feel spammy. Combine with `MaxDisplayCount` to put a hard cap on appearances.

## `Custom(predicate)`

For app-specific eligibility that the built-in rules don't cover. The predicate is a `suspend` lambda with a `TipContext` receiver:

```kotlin
val tip = Tip(
    id = "power_user_tip",
    title = "Power-user shortcut",
    message = "Try long-press to multi-select.",
    rules = listOf(
        TipRule.NotDismissed,
        TipRule.Custom {
            // `this` is TipContext — access tip, state, counters, nowMillis
            counters.eventCount("item_viewed") > 50 &&
                counters.screenVisitCount("settings") > 2
        },
    ),
)
```

`TipRule.Custom` is intentionally **not** a `data class` because Kotlin lambda equality is undefined — two `Custom` instances with identical predicates are not `==`.

## Combining rules

Rules are AND-ed together. There is no built-in OR. If you need OR semantics, combine them inside a single `Custom` rule:

```kotlin
TipRule.Custom {
    counters.eventCount("search_run") >= 5 ||
        counters.screenVisitCount("results") >= 3
}
```

## When a rule fails

`TipEvaluator.evaluate` returns `TipDecision.Hide(reason)` with a `TipHideReason` describing the failure:

| Rule | Hide reason on failure |
|------|------------------------|
| `NotDismissed` | `Dismissed` |
| `Once` | `AlreadyShownOnce` |
| `MaxDisplayCount` | `MaxDisplayCountReached` |
| `AfterEvent` | `EventCountNotReached(name, required, actual)` |
| `AfterScreenVisits` | `ScreenVisitCountNotReached(name, required, actual)` |
| `MinIntervalHours` | `MinIntervalNotReached(requiredHours, elapsedMillis)` |
| `Custom` | `CustomRuleFailed` |

Useful for debug overlays, analytics, or "Why isn't this tip showing?" inspector screens.
