package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The cart screen.
 */
public class CartPage {

    private final AppiumDriver driver;

    private final By header = AppiumBy.accessibilityId("test-YOUR CART");
    private final By checkoutButton = AppiumBy.accessibilityId("test-CHECKOUT");
    private final By removeButton = AppiumBy.accessibilityId("test-REMOVE");
    private final By cartItem = AppiumBy.accessibilityId("test-Item");

    public CartPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, header).isDisplayed();
    }

    public int itemCount() {
        return driver.findElements(cartItem).size();
    }

    public void removeFirstItem() {
        Waits.clickable(driver, removeButton).click();
    }

    public void checkout() {
        Waits.clickable(driver, checkoutButton).click();
    }
}
