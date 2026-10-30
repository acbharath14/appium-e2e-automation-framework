package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The "Checkout: Your Information" form.
 */
public class CheckoutInfoPage {

    private final AppiumDriver driver;

    private final By firstNameField = AppiumBy.accessibilityId("test-First Name");
    private final By lastNameField = AppiumBy.accessibilityId("test-Last Name");
    private final By postalCodeField = AppiumBy.accessibilityId("test-Zip/Postal Code");
    private final By continueButton = AppiumBy.accessibilityId("test-CONTINUE");
    private final By errorMessage = AppiumBy.accessibilityId("test-Error message");

    public CheckoutInfoPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, firstNameField).isDisplayed();
    }

    public void fillForm(String firstName, String lastName, String postalCode) {
        Waits.visible(driver, firstNameField).sendKeys(firstName);
        Waits.visible(driver, lastNameField).sendKeys(lastName);
        Waits.visible(driver, postalCodeField).sendKeys(postalCode);
    }

    public void continueCheckout() {
        Waits.clickable(driver, continueButton).click();
    }

    public String errorText() {
        return Waits.visible(driver, errorMessage).getText();
    }
}
