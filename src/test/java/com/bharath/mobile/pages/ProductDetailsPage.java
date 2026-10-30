package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

/**
 * The product details screen, reached by tapping a product in the list.
 */
public class ProductDetailsPage {

    private final AppiumDriver driver;

    private final By productName = AppiumBy.accessibilityId("test-Item title");
    private final By productPrice = AppiumBy.accessibilityId("test-Price");
    private final By addToCartButton = AppiumBy.accessibilityId("test-ADD TO CART");
    private final By backButton = AppiumBy.accessibilityId("test-BACK");

    public ProductDetailsPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, productName).isDisplayed();
    }

    public String name() {
        return Waits.visible(driver, productName).getText();
    }

    public String price() {
        return Waits.visible(driver, productPrice).getText();
    }

    public void addToCart() {
        Waits.clickable(driver, addToCartButton).click();
    }

    public void goBack() {
        Waits.clickable(driver, backButton).click();
    }
}
