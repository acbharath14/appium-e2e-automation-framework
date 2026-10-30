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
                            .setDeviceName(caps.getProperty("appium:deviceName"))
                            .setAvd(caps.getProperty("appium:avd"))
                            .setAvdLaunchTimeout(Duration.ofMillis(
                                    Long.parseLong(caps.getProperty("appium:avdLaunchTimeout"))))
                            .setApp(resolved.toString())
                            .setNewCommandTimeout(Duration.ofSeconds(
                                    Long.parseLong(caps.getProperty("appium:newCommandTimeout"))));
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

    private static Properties loadProperties(String platform) {
        String resource = "/" + platform + ".properties";
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
