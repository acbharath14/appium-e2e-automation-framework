package com.bharath.mobile.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

/**
 * Touch gestures built on the W3C Actions API.
 *
 * <p>Why W3C Actions and not {@code TouchAction}: the old TouchAction API was
 * removed in java-client 8. Any tutorial still teaching it is teaching a dead
 * API — this class shows the current one.
 */
public final class Gestures {

    private Gestures() {
        // utility class
    }

    /** Swipe up: reveals content below the fold (e.g. more products). */
    public static void scrollDown(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int centerX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        swipe(driver, centerX, startY, centerX, endY);
    }

    /** Swipe down: reveals content above the fold. */
    public static void scrollUp(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int centerX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        swipe(driver, centerX, startY, centerX, endY);
    }

    private static void swipe(AppiumDriver driver, int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(
                Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }
}
