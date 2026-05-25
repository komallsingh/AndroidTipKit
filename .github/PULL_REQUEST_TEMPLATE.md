# Summary

<!--
One short paragraph: what does this PR do and why?
Link related issues with "Closes #123" / "Refs #456".
-->

## Type of change

- [ ] Bug fix (non-breaking)
- [ ] New feature (non-breaking)
- [ ] Breaking change (public API removed or changed)
- [ ] Documentation only
- [ ] Build / tooling / infrastructure
- [ ] Refactor (no behavior change)

## Affected module(s)

- [ ] `nudgekit-core`
- [ ] `nudgekit-datastore`
- [ ] `nudgekit-compose`
- [ ] `sample`
- [ ] docs / repo meta

## Checklist

- [ ] I tested this locally on JDK 17.
- [ ] I updated docs (`README.md`, `docs/`, KDoc) if the change is user-facing.
- [ ] I added or updated tests if the change touches behavior.
- [ ] I did not introduce unnecessary dependencies.
- [ ] I did not break public API without explaining the migration in the PR description.
- [ ] I updated `CHANGELOG.md` under `[Unreleased]` if the change is user-visible.

## Verification commands

Please confirm the following all succeed locally before requesting review (JDK 17):

```bash
./gradlew :nudgekit-core:test
./gradlew :nudgekit-datastore:test
./gradlew :nudgekit-compose:build
./gradlew :sample:assembleDebug
./gradlew build
```

Paste the final lines of each command's output, or simply tick:

- [ ] `:nudgekit-core:test` — BUILD SUCCESSFUL
- [ ] `:nudgekit-datastore:test` — BUILD SUCCESSFUL
- [ ] `:nudgekit-compose:build` — BUILD SUCCESSFUL
- [ ] `:sample:assembleDebug` — BUILD SUCCESSFUL
- [ ] `./gradlew build` — BUILD SUCCESSFUL

## Screenshots / recordings

<!-- Required for any visible UI change. Light + dark mode if applicable. -->

## Additional notes

<!-- Tradeoffs, follow-ups, things reviewers should focus on. -->
