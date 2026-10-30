package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The "Checkout: Overview" screen — item total, tax, total, finish/cancel.
 */
public class CheckoutOverviewPage {

    private final AppiumDriver driver;

    private final By header = AppiumBy.accessibilityId("test-CHECKOUT: OVERVIEW");
    private final By itemTotal = AppiumBy.accessibilityId("test-Item total");
    private final By tax = AppiumBy.accessibilityId("test-Tax");
    private final By total = AppiumBy.accessibilityId("test-Total");
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

    public void finish() {
        Waits.clickable(driver, finishButton).click();
    }

    public void cancel() {
        Waits.clickable(driver, cancelButton).click();
    }
}
