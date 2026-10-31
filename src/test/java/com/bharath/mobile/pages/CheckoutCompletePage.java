package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The "Checkout: Complete" confirmation screen.
 */
public class CheckoutCompletePage {

    private final AppiumDriver driver;

    // v2.7.1: the exact header id is unconfirmed; match by the visible
    // "THANK YOU" text instead of a hardcoded accessibility id.
    private final By thankYouHeader =
            AppiumBy.xpath("//android.widget.TextView[contains(@text, 'THANK YOU')]");
    private final By backHomeButton = AppiumBy.accessibilityId("test-BACK HOME");

    public CheckoutCompletePage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, thankYouHeader).isDisplayed();
    }

    public void backHome() {
        Waits.clickable(driver, backHomeButton).click();
    }
}
