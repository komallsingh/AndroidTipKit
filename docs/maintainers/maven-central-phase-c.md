# Maven Central — Phase C (Maintainers Only)

> **Audience:** project maintainers with publish rights. This is a runbook for
> the remaining work to actually publish NudgeKit to Maven Central. It adds
> **no** build config — Phases A and B already wired publishing and gated
> signing. Nothing here should be committed with real secrets.

## 1. Current readiness state

Already done (Phases A + B):

- ✅ `./gradlew publishToMavenLocal` succeeds for the four library modules.
- ✅ **Sources JARs** (`-sources.jar`) produced per module.
- ✅ **Javadoc JARs** (`-javadoc.jar`) produced per module (empty-but-valid; Central only requires their presence).
- ✅ **POM metadata** complete: name, description, URL, Apache-2.0 license, developer, SCM.
- ✅ **In-memory GPG signing** wired and **gated** — skipped when no key is present, so builds/CI stay green.
- ✅ Coordinates: `io.github.abdullajon1881:<module>:<version>` (version centralized in `gradle.properties`).
- ✅ `sample` is excluded from publishing.

Not done yet (this phase):

- ❌ A **real GPG key** (only gated wiring exists).
- ❌ A **Central Portal account** and **namespace verification**.
- ❌ Any **remote upload** — nothing has been published anywhere.

## 2. Required accounts / access

| Need | Notes |
|------|-------|
| GitHub account owning `Abdullajon1881/AndroidTipKit` | Used to verify the `io.github.abdullajon1881` namespace. |
| Central Portal account | Sign up at <https://central.sonatype.com> (the modern "Central Portal", not the legacy OSSRH Nexus). |
| Namespace `io.github.abdullajon1881` | Register it in the Central Portal and verify ownership. |

**Why `io.github.*`:** the namespace `io.github.abdullajon1881` is verified by
proving control of the matching GitHub account (the Portal has you create a
short-lived verification repository). This avoids buying a domain and adding
DNS `TXT` records, which a custom group like `dev.nudgekit` would require.

## 3. GPG key steps

Generate a real key **on a trusted machine** (not in CI, not in the repo):

```bash
# Generate a key (choose RSA 4096 or Ed25519; use a STRONG passphrase).
gpg --full-generate-key

# List keys to find the long key id / fingerprint.
gpg --list-secret-keys --keyid-format=long
# sec   rsa4096/ABCDEF0123456789 2026-xx-xx ...
#       <FINGERPRINT>
# uid   Your Name <you@example.com>

# Export the PUBLIC key (shareable).
gpg --armor --export <KEY_ID> > nudgekit-public.asc

# Publish the public key so Central can verify signatures.
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
# (keys.openpgp.org is also commonly used)

# Export the ASCII-armored PRIVATE key for CI secret use.
# This is SECRET — handle like a password.
gpg --armor --export-secret-keys <KEY_ID> > nudgekit-private.asc
```

Rules:

- **Never commit** `nudgekit-private.asc`, the passphrase, or any keyring. Add them to `.gitignore` if they ever touch the working tree, and delete local copies once stored in Secrets.
- Use a **strong, unique passphrase**.
- The **public** key is safe to share; the **private** key and passphrase are not.
- For CI, the armored private key is stored as a GitHub Secret (see below). The build reads it via the in-memory signing properties already configured in Phase B.

## 4. GitHub Secrets

Store these as repository (or environment) secrets — never in the repo.
The signing names match the Phase B Gradle config (`ORG_GRADLE_PROJECT_*`
maps onto Gradle project properties automatically):

| Secret | Purpose |
|--------|---------|
| `ORG_GRADLE_PROJECT_signingInMemoryKey` | The full ASCII-armored **private** key (contents of `nudgekit-private.asc`). |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` | The key **passphrase**. |
| `ORG_GRADLE_PROJECT_signingInMemoryKeyId` | *(Optional)* the short/long key id, only needed when signing with a subkey. |
| `CENTRAL_USERNAME` *(placeholder)* | Central Portal **user token** username. Generate a token in the Portal — do not use your login password. |
| `CENTRAL_PASSWORD` *(placeholder)* | Central Portal **user token** password/value. |

> Secret names for the Central credentials are placeholders — finalize them
> when the upload mechanism is chosen (see §6). Generate **user tokens** in the
> Central Portal rather than using account credentials directly.

## 5. Local signing verification

Before any remote upload, prove the signed path works locally. Supply the key
via `-P` flags (or the `ORG_GRADLE_PROJECT_*` env vars) and publish to the
local Maven repo:

```bash
./gradlew publishToMavenLocal \
  -PsigningInMemoryKey="$(cat nudgekit-private.asc)" \
  -PsigningInMemoryKeyPassword="<passphrase>"
  # optional: -PsigningInMemoryKeyId="<key id>"
```

Then confirm `.asc` signatures now exist next to every artifact:

```bash
find ~/.m2/repository/io/github/abdullajon1881 -name "*.asc" | sort
```

You should see a `.asc` for each `.jar` / `.aar` / `-sources.jar` /
`-javadoc.jar` / `.pom` / `.module`. Without keys, **no** `.asc` files are
produced (the gate from Phase B) — that is the expected default state.

## 6. Central upload strategy (decision pending — do not implement yet)

Two viable paths. **Document the choice here before implementing; do not add a
plugin yet.**

**Option A — Manual bundle upload via Central Portal.**
Zip the signed artifacts into a Central-compliant bundle and upload through the
Portal UI / API. Pros: no new build dependency, full control, good for the
first release. Cons: manual, easy to get the bundle layout wrong.

**Option B — Gradle publish plugin (automated, later).**
Use a Central-publishing Gradle plugin to upload from `./gradlew` / CI. Pros:
repeatable, CI-friendly. Cons: adds a build dependency and release automation,
which the project has deliberately deferred. Revisit once the first manual
release has succeeded.

> No plugin is chosen yet. The first publish will most likely use **Option A**
> to keep automation out of the critical path; **Option B** can follow.

## 7. Pre-publish checklist

Run through this immediately before publishing a version:

- [ ] CI is green on `main`.
- [ ] `CHANGELOG.md` updated — the version section is filled in (moved out of `[Unreleased]`).
- [ ] `nudgekitVersion` in `gradle.properties` is correct (and **not** a stale `-SNAPSHOT`/alpha mismatch).
- [ ] Git tag created for the version (e.g. `vX.Y.Z`).
- [ ] GitHub release notes drafted (pre-release flag for alphas).
- [ ] `./gradlew publishToMavenLocal` passes.
- [ ] Signed artifacts verified locally — `.asc` files present for every artifact (§5).
- [ ] No secrets, keys, or passphrases tracked in git (`git status` clean of key files).
- [ ] Namespace `io.github.abdullajon1881` is verified in the Central Portal.

## 8. Rollback / failure notes

- **Releases to Maven Central are permanent.** Once a version is published, it
  **cannot** be edited, overwritten, or deleted.
- If the **upload fails** (bad bundle, missing signature, validation error), fix
  the problem and retry **before** the release is finalized — nothing is public
  until the Portal release step completes.
- If **bad artifacts are released** (wrong contents, missing files), do **not**
  attempt to overwrite them. Publish a **new version** (e.g. bump the alpha:
  `0.2.0-alpha.2`) with the fix and, if needed, document the bad version in the
  changelog.
- Alpha versions are still permanent — treat alpha publishing with the same care
  as a stable release.

## 9. Out of scope for this doc

- No Gradle build/config changes (Phases A + B already cover wiring).
- No Maven Central plugin added.
- No real credentials, keys, or automation committed.

When ready to execute, work top-to-bottom: accounts (§2) → key (§3) → secrets
(§4) → local signed verification (§5) → choose upload strategy (§6) →
pre-publish checklist (§7) → publish.
