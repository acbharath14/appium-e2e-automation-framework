package com.bharath.mobile.base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

/**
 * Builds the Appium driver for the requested platform.
 *
 * <p>Why this class exists: every capability the suite needs lives in
 * {@code src/test/resources/<platform>.properties}, so a newcomer can see
 * exactly what the driver is told without digging through code. The platform
 * comes from {@code -Dplatform=android|ios} (default: android).
 */
public final class DriverFactory {

    private DriverFactory() {
        // utility class
    }

    public static AppiumDriver createDriver() {
        String platform = System.getProperty("platform", "android").toLowerCase();
        String serverUrl = System.getProperty("appium.server.url", "http://127.0.0.1:4723");
        Properties caps = loadProperties(platform);

        // The `app` capability points at apps/ relative to the project root.
        // preflight.py downloads the binaries there; they are never committed.
        String appPath = caps.getProperty("appium:app");
        if (appPath == null || appPath.isBlank()) {
            throw new IllegalStateException(
                    "Missing 'appium:app' in " + platform + ".properties —"
                            + " keys containing ':' must be escaped as '\\:' in .properties files.");
        }
        Path resolved = Paths.get(System.getProperty("user.dir"), appPath).toAbsolutePath();
        if (!Files.exists(resolved)) {
            throw new IllegalStateException(
                    "App binary not found: " + resolved + ". Run scripts/preflight.py first.");
        }

        try {
            switch (platform) {
                case "android": {
                    UiAutomator2Options options = new UiAutomator2Options()
                            .setPlatformName(caps.getProperty("platformName"))
                            .setAutomationName(caps.getProperty("appium:automationName"))
                            .setDeviceName(caps.getProperty("appium:deviceName"));
                    // If an emulator is already running (CI's emulator runner,
                    // or one started by hand), use it — don't boot a second
                    // copy of the same AVD. Otherwise Appium boots the AVD
                    // itself, so nobody starts an emulator by hand.
                    if (emulatorRunning()) {
                        System.out.println(
                                "DriverFactory: emulator already running — skipping AVD launch");
                    } else {
                        options.setAvd(caps.getProperty("appium:avd"))
                                .setAvdLaunchTimeout(Duration.ofMillis(
                                        Long.parseLong(caps.getProperty("appium:avdLaunchTimeout"))));
                    }
                    options.setApp(resolved.toString())
                            .setNewCommandTimeout(Duration.ofSeconds(
                                    Long.parseLong(caps.getProperty("appium:newCommandTimeout"))));
                    String appWaitActivity = caps.getProperty("appium:appWaitActivity");
                    if (appWaitActivity != null && !appWaitActivity.isBlank()) {
                        options.setAppWaitActivity(appWaitActivity);
                    }
                    // Generous adb timeouts: a cold/slow emulator (CI especially)
                    // can take well over the 20s defaults to answer adb.
                    String adbExecTimeout = caps.getProperty("appium:adbExecTimeout");
                    if (adbExecTimeout != null && !adbExecTimeout.isBlank()) {
                        options.setAdbExecTimeout(Duration.ofMillis(
                                Long.parseLong(adbExecTimeout)));
                    }
                    String serverInstallTimeout =
                            caps.getProperty("appium:uiautomator2ServerInstallTimeout");
                    if (serverInstallTimeout != null && !serverInstallTimeout.isBlank()) {
                        options.setUiautomator2ServerInstallTimeout(Duration.ofMillis(
                                Long.parseLong(serverInstallTimeout)));
                    }
                    return new AndroidDriver(new URI(serverUrl).toURL(), options);
                }
                case "ios": {
                    XCUITestOptions options = new XCUITestOptions()
                            .setPlatformName(caps.getProperty("platformName"))
                            .setAutomationName(caps.getProperty("appium:automationName"))
                            .setDeviceName(caps.getProperty("appium:deviceName"))
                            .setPlatformVersion(caps.getProperty("appium:platformVersion"))
                            .setApp(resolved.toString())
                            .setNewCommandTimeout(Duration.ofSeconds(
                                    Long.parseLong(caps.getProperty("appium:newCommandTimeout"))));
                    return new IOSDriver(new URI(serverUrl).toURL(), options);
                }
                default:
                    throw new IllegalArgumentException(
                            "Unknown platform: " + platform + " (expected android or ios)");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start Appium driver: " + e.getMessage(), e);
        }
    }

    /**
     * True when an Android emulator/device is already connected via adb.
     * Used to decide whether Appium should boot the AVD itself or just use
     * what's running (CI's emulator runner, or a hand-started emulator).
     * Never throws: if adb can't be found or misbehaves, we assume nothing
     * is running and let Appium boot the AVD.
     */
    private static boolean emulatorRunning() {
        String adb = findAdb();
        if (adb == null) {
            return false;
        }
        try {
            Process p = new ProcessBuilder(adb, "devices").start();
            boolean finished = p.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            String out = new String(p.getInputStream().readAllBytes());
            // "List of devices attached" header, then one "<serial>\tdevice"
            // line per connected device.
            return out.lines().anyMatch(l -> l.trim().endsWith("\tdevice"));
        } catch (Exception e) {
            return false;
        }
    }

    /** Locate the adb binary via ANDROID_HOME, ANDROID_SDK_ROOT, or PATH. */
    private static String findAdb() {
        for (String home : new String[]{
                System.getenv("ANDROID_HOME"), System.getenv("ANDROID_SDK_ROOT")}) {
            if (home != null && !home.isBlank()) {
                Path candidate = Paths.get(home, "platform-tools", "adb");
                if (Files.isExecutable(candidate)) {
                    return candidate.toString();
                }
            }
        }
        // Fall back to PATH.
        for (String dir : System.getenv().getOrDefault("PATH", "").split(":")) {
            Path candidate = Paths.get(dir, "adb");
            if (Files.isExecutable(candidate)) {
                return candidate.toString();
            }
        }
        return null;
    }

    private static Properties loadProperties(String platform) {        String resource = "/" + platform + ".properties";
        Properties props = new Properties();
        try (InputStream in = DriverFactory.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing capabilities file: " + resource);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read capabilities file: " + resource, e);
        }
        return props;
    }
}
