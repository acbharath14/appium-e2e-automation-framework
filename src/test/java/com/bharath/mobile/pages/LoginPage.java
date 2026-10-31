package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Platform;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

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

    /**
     * The text of the login error banner, for negative-case assertions.
     * v2.7.1: test-Error message is a container with empty text; the actual
     * message is in a child TextView (verified via uiautomator dump).
     */
    public String errorText() {
        WebElement container = Waits.visible(driver, errorMessage);
        List<WebElement> textElements =
                container.findElements(AppiumBy.xpath(".//" + Platform.textElement()));
        if (textElements.isEmpty()) {
            return "";
        }
        return textElements.get(0).getText();
    }
}
