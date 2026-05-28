# Screenshots

This folder holds the screenshots referenced by the project [README](../../README.md)
"Screenshots" section. The images are **not committed yet** — this guide
describes what to capture and how, so the gallery renders once the PNGs are
added.

All screenshots come from the bundled [`sample`](../../sample/) app:

```bash
./gradlew :sample:assembleDebug
# install on an emulator/device and run, or launch from Android Studio
```

## Expected files

| File | Component | Mode | What it should show |
|------|-----------|------|---------------------|
| `inline-tip-light.png` | `InlineTip` | Light | An inline tip card with title, message, and an action button. |
| `inline-tip-dark.png` | `InlineTip` | Dark | The same inline tip in dark mode. |
| `tipbox-bottom-light.png` | `TipBox` (`TipPosition.Bottom`) | Light | A tip anchored **below** a button/content. |
| `tipbox-top-dark.png` | `TipBox` (`TipPosition.Top`) | Dark | A tip anchored **above** a button/content. |
| `managed-tip-flow-light.png` | `ManagedInlineTip` | Light | A managed, event-driven tip appearing after the rule is satisfied (e.g. the "Save Your Address" tip after visiting checkout twice). |

Keep these exact filenames — the README references them directly.

## Capture guidance

- **Device:** a clean, recent emulator for consistency — e.g. Pixel 7 / Pixel 8, API 34.
- **Orientation:** portrait.
- **Both themes:** toggle the system theme (Settings → Display → Dark theme, or the emulator quick setting) to capture light vs dark. The sample uses `MaterialTheme`, so it follows the system setting.
- **Framing:** capture just the relevant tip and its surrounding context — avoid full-screen shots dominated by empty space. Cropping to the meaningful area is fine.
- **Status bar:** optional; a clean status bar (demo mode) looks tidier but isn't required.

## Image optimization

- Prefer **PNG** (crisp UI) or **WebP** (smaller). If you use WebP, update the
  README references to match the extension.
- Target a sensible width (roughly **≤ 1080 px**); downscale oversized captures.
- Keep each file lean (aim for a few hundred KB or less). Run them through an
  optimizer (e.g. `pngquant`, `oxipng`, or `cwebp`) before committing.
- `.gitattributes` already treats `*.png` / `*.webp` as binary.

## After adding images

1. Drop the files into this folder with the exact names above.
2. Open the [README](../../README.md) locally (or on GitHub) and confirm the
   gallery renders and alt text is correct.
3. Remove the "placeholders until added" note from the README "Screenshots"
   section once the images are in.
