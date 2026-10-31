package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Gestures;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

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
    // v2.7.1: totals are text-only, no test-ids. starts-with avoids matching
    // "Item total:" when looking for "Total:".
    private final By itemTotal =
            AppiumBy.xpath("//android.widget.TextView[starts-with(@text, 'Item total:')]");
    private final By tax =
            AppiumBy.xpath("//android.widget.TextView[starts-with(@text, 'Tax:')]");
    private final By total =
            AppiumBy.xpath("//android.widget.TextView[starts-with(@text, 'Total:')]");
    private final By finishButton = AppiumBy.accessibilityId("test-FINISH");
    private final By cancelButton = AppiumBy.accessibilityId("test-CANCEL");

    public CheckoutOverviewPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, header).isDisplayed();
    }

    public String itemTotalText() {
        return Waits.visible(driver, itemTotal).getText();
    }

    public String taxText() {
        return Waits.visible(driver, tax).getText();
    }

    public String totalText() {
        return Waits.visible(driver, total).getText();
    }

    /** Scrolls the button into view (it sits below the fold) then taps it. */
    public void finish() {
        scrollToButton(finishButton);
        Waits.clickable(driver, finishButton).click();
    }

    /** Scrolls the button into view (it sits below the fold) then taps it. */
    public void cancel() {
        scrollToButton(cancelButton);
        Waits.clickable(driver, cancelButton).click();
    }

    private void scrollToButton(By button) {
        for (int i = 0; i < 3; i++) {
            if (!driver.findElements(button).isEmpty()) {
                return;
            }
            Gestures.scrollDown(driver);
        }
    }
}
