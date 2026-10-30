package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The Swag Labs login screen.
 *
 * <p>Why accessibility ids: the app exposes content-descriptions that mirror
 * the web app's data-test attributes (test-Username, test-Password,
 * test-LOGIN). Accessibility id is the most stable locator strategy on both
 * Android and iOS — it survives layout changes that would break XPath.
 */
public class LoginPage {

    private final AppiumDriver driver;

    private final By usernameField = AppiumBy.accessibilityId("test-Username");
    private final By passwordField = AppiumBy.accessibilityId("test-Password");
    private final By loginButton = AppiumBy.accessibilityId("test-LOGIN");
    private final By errorMessage = AppiumBy.accessibilityId("test-Error message");

    public LoginPage(AppiumDriver driver) {
        this.driver = driver;
    }

    /** True when the login screen is showing and ready for input. */
    public boolean isDisplayed() {
        return Waits.visible(driver, usernameField).isDisplayed();
    }

    public void loginAs(String username, String password) {
        Waits.visible(driver, usernameField).sendKeys(username);
        Waits.visible(driver, passwordField).sendKeys(password);
        Waits.clickable(driver, loginButton).click();
    }

    /** The text of the login error banner, for negative-case assertions. */
    public String errorText() {
        return Waits.visible(driver, errorMessage).getText();
    }
}
