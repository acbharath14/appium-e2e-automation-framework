package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Platform;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

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

    /**
     * v2.7.1: test-Error message is a container; the text is in a child TextView.
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
