package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Platform;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The "Checkout: Complete" confirmation screen.
 */
public class CheckoutCompletePage {

    private final AppiumDriver driver;

    // v2.7.1: the exact header id is unconfirmed; match by the visible
    // "THANK YOU" text via getText() filtering (platform-agnostic).
    private final By backHomeButton = AppiumBy.accessibilityId("test-BACK HOME");

    public CheckoutCompletePage(AppiumDriver driver) {
        this.driver = driver;
    }

    private WebElement findThankYouHeader() {
        List<WebElement> texts =
                driver.findElements(AppiumBy.xpath("//" + Platform.textElement()));
        for (WebElement t : texts) {
            String s = t.getText();
            if (s != null && s.contains("THANK YOU")) {
                return t;
            }
        }
        throw new java.util.NoSuchElementException("THANK YOU header not found");
    }

    public boolean isDisplayed() {
        return findThankYouHeader().isDisplayed();
    }

    public void backHome() {
        Waits.clickable(driver, backHomeButton).click();
    }
}
