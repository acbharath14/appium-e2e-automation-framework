package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Gestures;
import com.bharath.mobile.utils.Platform;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The "Checkout: Overview" screen — item total, tax, total, finish/cancel.
 *
 * <p>v2.7.1 locator notes (verified via uiautomator dumps 2026-10-31):
 * the totals have NO test-ids; they are TextViews with text like
 * "Item total: $29.99". FINISH and CANCEL exist but sit below the fold,
 * so the page scrolls before tapping them.
 */
public class CheckoutOverviewPage {

    private final AppiumDriver driver;

    private final By header = AppiumBy.accessibilityId("test-CHECKOUT: OVERVIEW");
    // v2.7.1: totals are text-only, no test-ids. Found via getText() filtering
    // (platform-agnostic) instead of XPath @text (Android-only).
    private final By finishButton = AppiumBy.accessibilityId("test-FINISH");
    private final By cancelButton = AppiumBy.accessibilityId("test-CANCEL");

    public CheckoutOverviewPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, header).isDisplayed();
    }

    /** Finds a text element by prefix, filtering via getText() (works on iOS/Android). */
    private String findTextByPrefix(String prefix) {
        List<WebElement> texts =
                driver.findElements(AppiumBy.xpath("//" + Platform.textElement()));
        for (WebElement t : texts) {
            String s = t.getText();
            if (s != null && s.startsWith(prefix)) {
                return s;
            }
        }
        throw new java.util.NoSuchElementException("No text starting with: " + prefix);
    }

    public String itemTotalText() {
        return findTextByPrefix("Item total:");
    }

    public String taxText() {
        // "Tax:" prefix avoids matching "Item total:" when looking for "Total:"
        // so check Tax before Total in the loop order (or use exact prefix)
        return findTextByPrefix("Tax:");
    }

    public String totalText() {
        // Must not match "Item total:" — the loop returns first match,
        // so we need to ensure "Total:" (without "Item ") is matched.
        // Using a more specific check: starts with "Total:" but not "Item total:"
        List<WebElement> texts =
                driver.findElements(AppiumBy.xpath("//" + Platform.textElement()));
        for (WebElement t : texts) {
            String s = t.getText();
            if (s != null && s.startsWith("Total:") && !s.startsWith("Item total:")) {
                return s;
            }
        }
        throw new java.util.NoSuchElementException("No text starting with: Total:");
    }

    /** Scrolls the button into view (it sits below the fold) then taps it. */
    public void finish() {
        revealBottomButtons();
        Waits.clickable(driver, finishButton).click();
    }

    /** Scrolls the button into view (it sits below the fold) then taps it. */
    public void cancel() {
        revealBottomButtons();
        Waits.clickable(driver, cancelButton).click();
    }

    /**
     * Swipes up three times to bring the bottom buttons into the viewport.
     * Unconditional (no findElements check) — the clickable wait that follows
     * handles timing; the check-then-scroll loop proved flaky for CANCEL.
     */
    private void revealBottomButtons() {
        for (int i = 0; i < 3; i++) {
            Gestures.scrollDown(driver);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
