package com.bharath.mobile.base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;

import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * Base class for every test class in the suite.
 *
 * <p>Lifecycle, and why it is shaped this way:
 * <ul>
 *   <li>One driver per test <b>class</b> ({@code @BeforeAll}): launching the
 *       app on an emulator costs 10–20s. A driver per test would make the
 *       suite painfully slow and teach the wrong lesson.</li>
 *   <li>App state reset between tests ({@code @BeforeEach}): terminate +
 *       activate is fast and deterministic — every test starts from the
 *       login screen without paying for a reinstall.</li>
 *   <li>Failure screenshots: {@link ScreenshotWatcher} (a JUnit 5
 *       {@code TestWatcher}) attaches a screenshot to the Allure report on
 *       failure. Failure triage lives in the report, never in terminal
 *       output.</li>
 * </ul>
 */
@ExtendWith(BaseTest.ScreenshotWatcher.class)
public abstract class BaseTest {

    protected static AppiumDriver driver;

    /** Bundle ids of the Swag Labs app, used for terminate/activate resets. */
    private static final String ANDROID_BUNDLE_ID = "com.swaglabsmobileapp";
    private static final String IOS_BUNDLE_ID = "com.saucelabs.SwagLabsMobileApp";

    @BeforeAll
    static void startDriver() {
        driver = DriverFactory.createDriver();
    }

    @AfterAll
    static void stopDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @BeforeEach
    void resetApp() {
        // Fast deterministic reset: kill the app and relaunch it, so each
        // test starts from the login screen without paying for a reinstall.
        String platform = System.getProperty("platform", "android").toLowerCase();
        String bundleId = platform.equals("ios") ? IOS_BUNDLE_ID : ANDROID_BUNDLE_ID;
        // terminateApp/activateApp live on InteractsWithApps, which both the
        // Android and iOS drivers implement — hence the cast, not a retype.
        InteractsWithApps apps = (InteractsWithApps) driver;
        apps.terminateApp(bundleId);
        apps.activateApp(bundleId);
    }

    /**
     * JUnit 5 watcher: on a failed test, capture a screenshot and attach it
     * to the Allure report under the test's name. Best-effort — a broken
     * screenshot must never fail the suite itself.
     */
    static class ScreenshotWatcher implements TestWatcher {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            if (driver == null) {
                return;
            }
            try {
                byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
                String name = "failure-screenshot-"
                        + context.getTestMethod().map(m -> m.getName()).orElse("unknown");
                Allure.addAttachment(name, "image/png",
                        new ByteArrayInputStream(screenshot), "png");
            } catch (Exception ignored) {
                // Screenshot is best-effort; never fail the suite over it.
            }
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            // nothing to do
        }
    }
}
