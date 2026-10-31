#!/usr/bin/env python3
"""Preflight checks for the Appium framework.

Verifies everything the suite needs, installing what's safe to install and
printing exact fix commands for the rest. Idempotent — safe to run often.

Skippable: SKIP_PREFLIGHT=1 mvn test

Checks, in order:
  1. Java 17+ and Maven on PATH
  2. Node 20+ and the Appium server (installs Appium + drivers if missing)
  3. ANDROID_HOME and the emulator binary
  4. The Pixel_7_API_34 AVD exists (prints the creation command if not)
  5. Swag Labs app binaries in apps/ (downloads them if absent)
  6. An Appium server listening on localhost:4723 (starts one if missing)

CI mode (--ci): the GitHub workflow runs on reactivecircus/android-emulator-runner,
which provides its own emulator AFTER this script runs. So --ci skips the
ANDROID_HOME check, the AVD check, the xcuitest driver install (Linux runners
can't run iOS simulators), and starting the Appium server — the emulator step
starts the server itself so it inherits the runner's ANDROID_SDK_ROOT.

App-only mode (--app-only): for cloud-device CI (BrowserStack), where the
tests talk directly to the cloud hub and no local Appium server, drivers,
or emulator are needed. Only ensures the sample app binaries exist.
"""

import json
import os
import re
import shutil
import socket
import subprocess
import sys
import time
import urllib.request

APPS_DIR = os.path.join(os.getcwd(), "apps")
ANDROID_APK = os.path.join(APPS_DIR, "Android.SauceLabs.Mobile.Sample.app.apk")
IOS_APP_ZIP = os.path.join(APPS_DIR, "iOS.Simulator.SauceLabs.Mobile.Sample.app.zip")
SAMPLE_APP_RELEASES_API = (
    "https://api.github.com/repos/saucelabs/sample-app-mobile/releases/latest"
)
AVD_NAME = "Pixel_7_API_34"
APPIUM_PORT = 4723

failures = []


def run(cmd, timeout=120, **kwargs):
    """Run a command, returning (ok, combined output). Never raises."""
    try:
        out = subprocess.run(
            cmd, capture_output=True, text=True, timeout=timeout, **kwargs)
        # appium (and npm under it) report errors on stderr — capture both.
        combined = (out.stdout.strip() + "\n" + out.stderr.strip()).strip()
        return out.returncode == 0, combined
    except Exception as e:  # missing binary, timeout, ...
        return False, str(e)


def check(name, ok, fix):
    status = "ok" if ok else "MISSING"
    print(f"[{status}] {name}")
    if not ok:
        failures.append(name)
        print(f"        fix: {fix}")


def java_version_ok():
    # java -version writes to stderr, so capture both streams.
    try:
        out = subprocess.run(["java", "-version"], capture_output=True,
                             text=True, timeout=30)
    except Exception:
        return False
    if out.returncode != 0:
        return False
    m = re.search(r'version "(\d+)', out.stderr + out.stdout)
    return bool(m and int(m.group(1)) >= 17)


def main():
    ci_mode = "--ci" in sys.argv[1:]
    app_only = "--app-only" in sys.argv[1:]
    if app_only:
        print("Appium framework preflight (app-only mode: cloud CI)")
    elif ci_mode:
        print("Appium framework preflight (CI mode: emulator provided by runner)")
    else:
        print("Appium framework preflight")
    print("==========================")
    os.makedirs(APPS_DIR, exist_ok=True)

    if not app_only:
        # 1. Java + Maven
        check("Java 17+", java_version_ok(),
              "install Temurin 17: https://adoptium.net/temurin/releases/")
        check("Maven", shutil.which("mvn") is not None,
              "install Maven 3.9+: https://maven.apache.org/download.cgi")

        # 2. Node + Appium server + drivers
        ok, out = run(["node", "--version"])
        node_ok = ok and int(re.search(r"v(\d+)", out).group(1)) >= 20
        check("Node.js 20+", node_ok, "install Node 20+: https://nodejs.org/")
        if node_ok and shutil.which("appium") is None:
            print("  installing Appium server (npm install -g appium)...")
            ok, _ = run(["npm", "install", "-g", "appium"])
            if not ok:
                print("  npm install failed; rerun preflight after fixing npm")
        check("Appium server", shutil.which("appium") is not None,
              "npm install -g appium")
        # CI runners are Linux: the xcuitest driver can never run there, so don't
        # waste minutes installing it.
        drivers = ("uiautomator2",) if ci_mode else ("uiautomator2", "xcuitest")
        for driver in drivers:
            ok, out = run(["appium", "driver", "list", "--installed"])
            has = ok and driver in out
            if not has and shutil.which("appium"):
                print(f"  installing driver {driver} (can take a few minutes)...")
                ok, out = run(["appium", "driver", "install", driver], timeout=600)
                if not ok:
                    # Show the real error instead of swallowing it.
                    print(f"  driver install failed:\n{out}")
                # Re-check the installed list rather than trusting the exit code.
                ok2, out2 = run(["appium", "driver", "list", "--installed"])
                has = ok2 and driver in out2
            check(f"Appium driver {driver}", has,
                  f"appium driver install {driver}")

    # 3. Android SDK (skipped in CI: the emulator runner provides it after us;
    # skipped in app-only mode: cloud devices need no local SDK)
    android_home = os.environ.get("ANDROID_HOME", "")
    emulator = os.path.join(android_home, "emulator", "emulator")
    if ci_mode or app_only:
        print("[skip] ANDROID_HOME with emulator (not needed in this mode)")
        print(f"[skip] AVD {AVD_NAME} (not needed in this mode)")
    else:
        check("ANDROID_HOME with emulator", bool(android_home)
              and os.path.isfile(emulator),
              "install Android Studio or cmdline-tools and export ANDROID_HOME")

        # 4. AVD exists
        avd_ok = False
        if os.path.isfile(emulator):
            ok, out = run([emulator, "-list-avds"])
            avd_ok = ok and AVD_NAME in out
        check(f"AVD {AVD_NAME}", avd_ok,
              f'avdmanager create avd -n {AVD_NAME} -k '
              '"system-images;android-34;google_apis;x86_64" -d "pixel_7"')

    # 5. Sample app binaries (downloaded, never committed)
    download_apps()

    # 6. Appium server listening (skipped in CI: the emulator step starts it
    # itself so it inherits the runner's ANDROID_SDK_ROOT; skipped in
    # app-only mode: cloud CI needs no local server at all)
    if app_only:
        print(f"[skip] Appium server on :{APPIUM_PORT} (app-only: not needed)")
    elif ci_mode:
        print(f"[skip] Appium server on :{APPIUM_PORT} (CI: started in emulator step)")
    elif not port_open("127.0.0.1", APPIUM_PORT):
        if shutil.which("appium"):
            print(f"  starting Appium server on :{APPIUM_PORT}...")
            subprocess.Popen(["appium", "--port", str(APPIUM_PORT)],
                             stdout=subprocess.DEVNULL,
                             stderr=subprocess.DEVNULL)
            for _ in range(30):
                if port_open("127.0.0.1", APPIUM_PORT):
                    break
                time.sleep(1)
    if not ci_mode and not app_only:
        check(f"Appium server on :{APPIUM_PORT}",
              port_open("127.0.0.1", APPIUM_PORT),
              f"appium --port {APPIUM_PORT}")

    print()
    if failures:
        print(f"Preflight FAILED: {len(failures)} check(s) need attention: "
              + ", ".join(failures))
        return 1
    print("Preflight green. To run the Android suite:")
    print("    mvn test")
    print("For iOS (macOS only):")
    print("    mvn test -Dplatform=ios")
    return 0


def port_open(host, port):
    try:
        with socket.create_connection((host, port), timeout=2):
            return True
    except OSError:
        return False


def download_apps():
    """Fetch Swag Labs Android + iOS builds into apps/ if absent."""
    need_android = not os.path.isfile(ANDROID_APK)
    need_ios = not os.path.isfile(IOS_APP_ZIP)
    if not (need_android or need_ios):
        check("sample app binaries", True, "")
        return
    print("  downloading Swag Labs sample app builds...")
    try:
        req = urllib.request.Request(
            SAMPLE_APP_RELEASES_API,
            headers={"User-Agent": "appium-preflight"})
        with urllib.request.urlopen(req, timeout=60) as resp:
            release = json.load(resp)
        assets = {a["name"]: a["browser_download_url"]
                  for a in release.get("assets", [])}
        apk = next((u for n, u in assets.items() if n.endswith(".apk")), None)
        ipa = next((u for n, u in assets.items()
                    if "simulator" in n.lower() and n.endswith(".zip")), None)
        if need_android and apk:
            fetch(apk, ANDROID_APK)
        if need_ios and ipa:
            fetch(ipa, IOS_APP_ZIP)
    except Exception as e:
        print(f"  download failed: {e}")
    check("sample app binaries",
          os.path.isfile(ANDROID_APK) and os.path.isfile(IOS_APP_ZIP),
          "download manually from "
          "https://github.com/saucelabs/sample-app-mobile/releases "
          f"into {APPS_DIR}/")


def fetch(url, dest):
    req = urllib.request.Request(url, headers={"User-Agent": "appium-preflight"})
    with urllib.request.urlopen(req, timeout=300) as resp, \
            open(dest, "wb") as f:
        shutil.copyfileobj(resp, f)
    print(f"  saved {os.path.basename(dest)}")


if __name__ == "__main__":
    sys.exit(main())
