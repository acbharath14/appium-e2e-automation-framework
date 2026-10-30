package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Gestures;
import com.bharath.mobile.utils.Waits;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * The product catalog screen (landing screen after login).
 *
 * <p>Why this page matters for learning: it is where scrolling enters the
 * picture. Products below the fold don't exist to the driver until scrolled
 * into view, so {@link #scrollToProduct(String)} shows the standard
 * scroll-until-visible pattern instead of a fixed sleep.
 */
public class ProductsPage {

    private final AppiumDriver driver;

    private final By header = AppiumBy.accessibilityId("test-PRODUCTS");
    private final By cartBadge = AppiumBy.accessibilityId("test-Cart");
    private final By menuButton = AppiumBy.accessibilityId("test-Menu");
    private final By sortButton = AppiumBy.accessibilityId("test-Modal Selector Button");
    private final By productNames = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='test-Item title']");

    public ProductsPage(AppiumDriver driver) {
        this.driver = driver;
    }

    public boolean isDisplayed() {
        return Waits.visible(driver, header).isDisplayed();
    }

    /** Adds the named product to the cart from the list. */
    public void addToCart(String productName) {
        scrollToProduct(productName);
        By addButton = AppiumBy.accessibilityId("test-ADD TO CART");
        Waits.clickable(driver, addButton).click();
    }

    /** Number shown on the cart badge; 0 when the badge is absent. */
    public int cartBadgeCount() {
        List<WebElement> badges = driver.findElements(cartBadge);
        if (badges.isEmpty()) {
            return 0;
        }
        String text = badges.get(0).getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text.trim());
    }

    public void openCart() {
        Waits.clickable(driver, cartBadge).click();
    }

    public void openProductDetails(String productName) {
        scrollToProduct(productName);
        Waits.clickable(driver, AppiumBy.accessibilityId("test-Item-" + productName)).click();
    }

    /** Scrolls until the product name is visible (max 5 swipes). */
    public void scrollToProduct(String productName) {
        By name = AppiumBy.xpath(
                "//android.widget.TextView[@content-desc='test-Item title' and @text='"
                        + productName + "']");
        for (int i = 0; i < 5; i++) {
            if (!driver.findElements(name).isEmpty()) {
                return;
            }
            Gestures.scrollDown(driver);
        }
        throw new IllegalStateException("Product not found after scrolling: " + productName);
    }

    public int visibleProductCount() {
        return driver.findElements(productNames).size();
    }

    public void openMenu() {
        Waits.clickable(driver, menuButton).click();
    }
}
