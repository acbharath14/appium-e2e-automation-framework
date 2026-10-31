package com.bharath.mobile.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Explicit waits. Page objects go through here — never {@code Thread.sleep}.
 *
 * <p>Why: mobile drivers are slow and timing varies wildly between a cold
 * emulator boot and a warm one. Sleeping a fixed amount either wastes time
 * or flakes. Waiting for a condition is the only approach that survives
 * both.
 */
public final class Waits {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private Waits() {
        // utility class
    }

    public static WebElement visible(AppiumDriver driver, By locator) {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement clickable(AppiumDriver driver, By locator) {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean invisible(AppiumDriver driver, By locator) {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static WebElement visible(AppiumDriver driver, By locator, Duration timeout) {
        return new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until a custom condition yields a non-null element. For lookups
     * that need more than a plain locator — e.g. finding a child element off
     * a freshly re-located parent on every poll.
     */
    public static WebElement until(AppiumDriver driver,
            java.util.function.Function<WebDriver, WebElement> condition) {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT).until(condition);
    }
}
