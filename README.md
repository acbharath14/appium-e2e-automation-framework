# appium-e2e-automation-framework

Native mobile UI test automation with [Appium](https://appium.io), Java 17,
and JUnit 5 — against Android emulators and iOS simulators, with Allure
execution reports.

If you're new to mobile test automation, start with
[How Appium works](#how-appium-works-30-second-model) below, then run the
suite. Every test is written to be read: plain-words titles, page objects
with why-comments, no magic.

## How Appium works (30-second model)

Your test code (Java) → the **Appium server** (Node.js, `localhost:4723`) →
a **platform driver** (UiAutomator2 on Android, XCUITest on iOS) → the
**emulator or simulator**. Everything the driver needs is declared as
**capabilities** — key/value pairs like `platformName=Android`,
`appium:automationName=UiAutomator2`, `appium:app=<path to the .apk>`.
You'll find ours in `src/test/resources/android.properties` and
`ios.properties`, one field per line, each explained where it's
non-obvious.

Two terms: Android has **emulators**, iOS has **simulators**. Both are free,
and this framework boots them itself — you never start one by hand.

## The sample app

[Swag Labs](https://github.com/saucelabs/sample-app-mobile) by Sauce Labs:
the same shop app built for **both** Android (`.apk`) and iOS (simulator
`.app.zip`) from one codebase, with real login / products / cart / checkout
flows. Credentials mirror the saucedemo web app: `standard_user` /
`secret_sauce` is valid, `locked_out_user` is rejected.

The binaries are **downloaded, never committed**: `scripts/preflight.py`
fetches them from the Sauce Labs releases page into `apps/` (gitignored)
on first run.

## Prerequisites

| Need | Fix command / link |
|---|---|
| Java 17+ | [Temurin 17](https://adoptium.net/temurin/releases/) |
| Maven 3.9+ | [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) |
| Node.js 20+ | [nodejs.org](https://nodejs.org/) (for the Appium server) |
| Android SDK | Android Studio or cmdline-tools; export `ANDROID_HOME` |
| Xcode 16+ | Mac App Store (iOS runs only) |

`scripts/preflight.py` checks all of this, installs the Appium server and
its drivers for you (`uiautomator2`, `xcuitest`), and prints the exact fix
command for anything it can't install.

## Quick start

```bash
git clone https://github.com/acbharath14/appium-e2e-automation-framework.git
cd appium-e2e-automation-framework
python3 scripts/preflight.py
mvn test                # Android emulator (default)
mvn test -Dplatform=ios # iOS simulator (macOS only)
```

The preflight boots nothing yet — it only verifies. The first `mvn test`
takes a few minutes: Appium cold-boots the emulator (`avdLaunchTimeout`
covers it), installs the app, and runs the suite.

Skip the preflight once everything is green: `SKIP_PREFLIGHT=1 mvn test`.
(It doesn't run inside Maven automatically — run it by hand; it's the
documented first step, not hidden magic.)

## The 15 tests (what each one teaches)

Titles state behavior + expected outcome in plain words.

**Login** (`LoginTests`) — valid login lands on products; wrong password
shows the error banner; locked-out user shows its error.

**Catalog** (`CatalogTests`) — products list with names and prices; tapping
a product opens details; scrolling reveals off-screen products (the
scroll-until-visible pattern, not a sleep).

**Cart** (`CartTests`) — add one product, badge shows 1; badge matches the
added count; removing empties the cart.

**Checkout** (`CheckoutTests`) — empty form is rejected with field errors;
overview shows item total, tax, total; finish shows the thank-you screen;
cancel returns to products.

**Gestures & app state** (`GesturesTests`) — scroll down/up keeps the list;
backgrounding and foregrounding the app keeps the cart.

## Project tour

- `src/test/java/com/bharath/mobile/base/DriverFactory.java` — builds the
  driver from the `.properties` capabilities; resolves the app binary path.
- `src/test/java/com/bharath/mobile/base/BaseTest.java` — lifecycle: one
  driver per test **class** (app launch is expensive), terminate+activate
  reset between tests (fast and deterministic), Allure screenshot on
  failure via a JUnit 5 `TestWatcher`.
- `src/test/java/com/bharath/mobile/pages/` — one page object per screen.
  Locators prefer **accessibility id** (the app's content-descriptions
  mirror the web `data-test` attributes) because it survives layout
  changes on both platforms.
- `src/test/java/com/bharath/mobile/utils/Waits.java` — explicit waits.
  `Thread.sleep` appears nowhere in this repo, on purpose.
- `src/test/java/com/bharath/mobile/utils/Gestures.java` — touch gestures
  on the W3C Actions API. (The old `TouchAction` API was removed in
  java-client 8 — tutorials still teaching it are out of date.)
- `src/test/resources/*.properties` — capabilities, one platform per file.

## Reports

```bash
mvn test
npx -y allure-commandline serve target/allure-results   # execution report
open target/site/jacoco/index.html                      # coverage report
```

Failure screenshots attach to the failing test in Allure automatically.
Triage happens in the report, not in terminal output.

## CI

`.github/workflows/e2e.yml` runs the **Android** suite on GitHub Actions
(`ubuntu-latest` + `reactivecircus/android-emulator-runner`, API 34),
uploads the Allure results and the JaCoCo report as artifacts.

**iOS stays local-only**, deliberately: macOS runners are slow and
expensive, and claiming iOS-on-CI without it would be aspirational. The
README would rather be honest than impressive. Run it locally with
`mvn test -Dplatform=ios`.

(The workflow file is pasted via the GitHub web UI — the automation app
can't push `.github/workflows/` — so it lives ready-to-paste, not as a
claim.)

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `App binary not found` | Run `python3 scripts/preflight.py` — it downloads the Swag Labs builds into `apps/`. |
| Emulator never boots | Cold boot can take minutes on first run; `avdLaunchTimeout` is 180s. Check `emulator -list-avds` shows `Pixel_7_API_34`. |
| `Connection refused :4723` | No Appium server running — preflight starts one; or run `appium` by hand. |
| iOS: `platformVersion` mismatch | Match it to an installed simulator runtime (`xcrun simctl list runtimes`). |
| Flaky first test after boot | The emulator is still settling; the `@BeforeAll` driver start plus explicit waits absorb it. Don't add sleeps. |

## What's next (documented, not built)

- **BrowserStack App Automate** as the cloud path: same tests, capabilities
  pointed at the device farm instead of the local emulator.
- Real-device runs (USB/WiFi); note iOS real devices need Apple Developer
  signing.

## License

MIT — see [LICENSE](LICENSE).
