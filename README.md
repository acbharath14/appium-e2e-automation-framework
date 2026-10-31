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

Two terms: Android has **emulators**, iOS has **simulators**. Both are free.
If one is already running, the suite uses it; otherwise Appium boots it
itself — you never start one by hand.

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
  driver from the `.properties` capabilities; resolves the app binary path;
  checks `adb devices` first and uses a running emulator when one is
  connected, otherwise boots the AVD itself.
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

`.github/workflows/e2e.yml` runs the **Android** suite on GitHub Actions on
every push and pull request to `main`, on a **self-hosted runner on the
iMac** (`runs-on: [self-hosted, macos, imac]`). The workflow:

1. Runs `python3 scripts/preflight.py` (full mode) — verifies the Android
   SDK, the `Pixel_7_API_34` AVD, the Appium server and drivers, and
   downloads the sample app; idempotent, installs only what's missing and
   starts the Appium server if it isn't listening.
2. Runs `mvn -B test -Dplatform=android`. `DriverFactory` uses an
   already-running emulator if one is up, otherwise boots the AVD itself.
3. Uploads the Allure results and the JaCoCo report as artifacts (always,
   even on failure).

**One-time runner setup** (on the iMac, as your normal user — not root,
so it sees your `~/.android` AVDs and SDK): repo → Settings → Actions →
Runners → New self-hosted runner → macOS, then run the download/configure
commands it shows, adding the `imac` label at `./config.sh` time. Start
with `./run.sh` to verify, then keep it running (a `~/Library/LaunchAgents`
plist works for auto-start on login). The iMac must be on, logged in, and
awake — jobs queue until it is.

**Why not a cloud emulator:** tried on `ubuntu-latest` with
`reactivecircus/android-emulator-runner` and retired — cold boots took
10–16 minutes and every adb/Appium operation timed out on the shared
runners. **Why not a device farm:** Firebase Test Lab doesn't support
Appium (and shuts down Sep 2027); BrowserStack/Sauce Labs need a paid plan
for a private repo. `DriverFactory` still has a `-Dplatform=browserstack`
mode for manual cloud runs (credentials via `-Dbrowserstack.user/key`,
app via `-Dbrowserstack.app=bs://...`).

**iOS stays local-only**, deliberately: macOS runners bill at 10x per minute
and every run would download the ~9GB simulator runtime, for a suite that is
9 passing tests plus 6 intentional skips (the app's own accessibility gaps).
Run it locally with `mvn test -Dplatform=ios`.

There is no GitLab CI for this repo, also deliberately: GitHub Actions covers
the suite, and the iMac's GitLab runner is a Docker executor — an Android
emulator won't run inside Docker on macOS.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `App binary not found` | Run `python3 scripts/preflight.py` — it downloads the Swag Labs builds into `apps/`. |
| Emulator never boots | Cold boot can take minutes on first run; `avdLaunchTimeout` is 180s. Check `emulator -list-avds` shows `Pixel_7_API_34`. |
| `Connection refused :4723` | No Appium server running — preflight starts one; or run `appium` by hand. |
| iOS: `platformVersion` mismatch | Match it to an installed simulator runtime (`xcrun simctl list runtimes`). |
| Flaky first test after boot | The emulator is still settling; the `@BeforeAll` driver start plus explicit waits absorb it. Don't add sleeps. |
| Appium server can't find Android SDK | The server inherits env from the shell that started it — launch `appium` from a terminal with `ANDROID_HOME` exported. If it was started from an IDE or a bare shell, `kill $(lsof -ti:4723)` and relaunch from a proper terminal. |
| Android app never launches (stuck on splash) | Missing `appium:appWaitActivity` — the capability is `com.swaglabsmobileapp.*` in `android.properties`. Without it Appium gives up waiting for the main activity. |
| Properties file: `appium:` keys ignored | `java.util.Properties` treats unescaped `:` as a key separator — write them as `appium\:automationName` in the `.properties` files. |
| `NoSuchMethodError` from Selenium classes | `selenium-support` must match the Selenium version bundled in `java-client` 11.x — it's pinned explicitly in `pom.xml` (test scope). Don't bump one without the other. |
| Background/foreground test flaky on slow machines | `GesturesTests.backgroundForegroundKeepsCart` backgrounds the app for 3s. On a loaded host the relaunch can exceed the implicit wait — the test is timing-sensitive by nature, not a logic bug. Re-run it in isolation before investigating. |

## Locator notes (v2.7.1)

Swag Labs 2.7.1 is a React Native app; testIDs are built at runtime as
`'test-' + i18nKey`. Verified via uiautomator dumps (Android):

- Login: `test-Username`, `test-Password`, `test-LOGIN`. The error container
  `test-Error message` is a `ViewGroup` with empty text — the message lives in
  a child `TextView`.
- Products: `test-Menu`, `test-Cart`, `test-PRODUCTS`, per-row `test-Item`
  (not clickable — details open via the clickable `ViewGroup` that contains
  the title; located with one atomic XPath so the title and its wrapper are
  always read from the same tree snapshot), `test-Item title`, `test-Price`,
  `test-ADD TO CART`.
  There is **no** `test-Item-<name>` id. The cart badge count is a child
  `TextView` inside `test-Cart`; `test-Cart` itself is not clickable — the
  framework taps its bounds center via W3C Actions.
- Cart: header `test-Cart Content`, `test-CHECKOUT`, `test-CONTINUE SHOPPING`,
  `test-REMOVE`, `test-Amount`, `test-Description`.
- Checkout info: `test-Checkout: Your Info`, `test-First Name`,
  `test-Last Name`, `test-Zip/Postal Code`, `test-CONTINUE`, `test-CANCEL`.
- Overview: `test-CHECKOUT: OVERVIEW`; the totals have **no** test-ids
  (plain `TextView`s like `"Item total: $29.99"`) — located by text. `test-FINISH`
  sits below the fold; the page object scrolls before tapping it.
- Details: marker `test-Inventory item page`, `test-BACK TO PRODUCTS`.
  `name()` returns the first non-empty `TextView` that isn't a `$` price and
  isn't all-uppercase.

## iOS limitations (v2.7.1)

The iOS app (v2.7.1) has accessibility gaps that limit automation:

- **Cart button not tappable**: `test-Cart` has `accessible=false`. XCUITest
  cannot tap non-accessible elements — 7 methods tried (direct click, W3C
  Actions, `mobile: tap`, parent-wrapper tap, badge-child tap). Manual taps
  work (they bypass XCUITest hit-testing). Cart-navigation tests
  (`CartTests.removeProductFromCart`, all `CheckoutTests`) are skipped on iOS
  via `Assumptions.assumeFalse(Platform.isIOS(), ...)`.
- **Product-details ancestor**: XCUITest does not support the `ancestor::`
  XPath axis. `CatalogTests.tappingProductOpensDetails` is skipped on iOS.
- **Badge count**: On iOS the count is in the `label` attribute of `test-Cart`
  (e.g. `label="1"`), not a child `StaticText`. `ProductsPage.cartBadgeCount()`
  reads the attribute on iOS.
- **Deep links**: The app registers `swaglabs://` with React Navigation paths
  (`cart/:ids`, etc.), but `mobile: deepLink` to `swaglabs://cart` does not
  navigate (the `:ids` param format is unknown).

Working on iOS (9/15): all `LoginTests` (3), cart badge tests (2),
`GesturesTests` (2), catalog tests except product-details (2).

## What's next (documented, not built)

- Parallelize the BrowserStack run (shard across multiple devices; the
  suite is 15 tests and would fit a 2–3 device matrix).
- Real-device runs (USB/WiFi); note iOS real devices need Apple Developer
  signing.

## License

MIT — see [LICENSE](LICENSE).
