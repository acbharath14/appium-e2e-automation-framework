package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The product details screen, reached by tapping a product in the list.
 *
 * <p>v2.7.1 locator notes (verified via uiautomator dumps 2026-10-31):
 * the details screen has no test-Item title; the product name is a TextView
 * without a test-id. test-Price is a container — the amount is in a child
 * TextView. The back button is test-BACK TO PRODUCTS (not test-BACK).
 */
public class ProductDetailsPage {

    private final AppiumDriver driver;

    private final By detailsMarker = AppiumBy.accessibilityId("test-Inventory item page");
    private final By priceContainer = AppiumBy.accessibilityId("test-Price");
    private final By addToCartButton = AppiumBy.accessibilityId("test-ADD TO CART");
    private final By backButton = AppiumBy.accessibilityId("test-BACK TO PRODUCTS");

    public ProductDetailsPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, detailsMarker).isDisplayed();
    }

    /**
     * The product name shown on the details screen.
     * v2.7.1: the title TextView carries no test-id. Returns the first
     * non-empty TextView that is not a dollar price and not an all-uppercase
     * button label (e.g. "BACK TO PRODUCTS").
     */
    public String name() {
        Waits.visible(driver, detailsMarker);
        List<WebElement> texts =
                driver.findElements(AppiumBy.xpath("//android.widget.TextView"));
        for (WebElement t : texts) {
            String s = t.getText();
            if (s == null) {
                continue;
            }
            s = s.trim();
            if (s.isEmpty() || s.startsWith("$")) {
                continue;
            }
            // Skip all-uppercase button labels like "BACK TO PRODUCTS"
            if (s.equals(s.toUpperCase())) {
                continue;
            }
            return s;
        }
        return "";
    }

    /** The price, read from the child TextView of the test-Price container. */
    public String price() {
        WebElement container = Waits.visible(driver, priceContainer);
        List<WebElement> amounts =
                container.findElements(AppiumBy.xpath(".//android.widget.TextView"));
        if (amounts.isEmpty()) {
            return "";
        }
        return amounts.get(0).getText();
    }

    public void addToCart() {
        Waits.clickable(driver, addToCartButton).click();
    }

    public void goBack() {
        Waits.clickable(driver, backButton).click();
    }
}
