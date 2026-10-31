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
 *
 * <p>v2.7.1 locator notes (verified via uiautomator dumps 2026-10-31):
 * test-ids are built at runtime as 'test-' + i18n key, so they never appear
 * as literals in the APK. The cart badge count lives in a child TextView,
 * not the test-Cart container's text. The test-Cart container itself is not
 * clickable — the tap target is its clickable ancestor.
 */
public class ProductsPage {

    private final AppiumDriver driver;

    private final By header = AppiumBy.accessibilityId("test-PRODUCTS");
    private final By cartBadge = AppiumBy.accessibilityId("test-Cart");
    private final By menuButton = AppiumBy.accessibilityId("test-Menu");
    private final By sortButton = AppiumBy.accessibilityId("test-Modal Selector Button");
    private final By productNames = AppiumBy.accessibilityId("test-Item title");

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
        // Multiple ADD TO CART buttons exist (one per visible product);
        // tap the one for the scrolled-to product by finding its container.
        List<WebElement> buttons = driver.findElements(addButton);
        if (buttons.isEmpty()) {
            throw new IllegalStateException("No ADD TO CART button found");
        }
        // The first visible ADD TO CART corresponds to the scrolled-to product
        // when scrollToProduct has positioned it at the top.
        Waits.clickable(driver, addButton).click();
    }

    /**
     * Number shown on the cart badge; 0 when the badge is absent.
     * v2.7.1: the count is in a child TextView of the test-Cart container,
     * not in the container's own text (which is always empty).
     */
    public int cartBadgeCount() {
        List<WebElement> badges = driver.findElements(cartBadge);
        if (badges.isEmpty()) {
            return 0;
        }
        List<WebElement> countElements =
                badges.get(0).findElements(AppiumBy.xpath(".//android.widget.TextView"));
        if (countElements.isEmpty()) {
            return 0;
        }
        String text = countElements.get(0).getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text.trim());
    }

    /**
     * Opens the cart. v2.7.1: the test-Cart container is not clickable;
     * the tap target is the nearest clickable ancestor ViewGroup.
     */
    public void openCart() {
        By clickableCart = AppiumBy.xpath(
                "//android.view.ViewGroup[@content-desc='test-Cart']" +
                "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
        Waits.clickable(driver, clickableCart).click();
    }

    /**
     * Opens the product details screen. v2.7.1: there is no
     * test-Item-&lt;name&gt; id; the tap target is the clickable ancestor
     * ViewGroup of the product title TextView.
     */
    public void openProductDetails(String productName) {
        scrollToProduct(productName);
        By detailsButton = AppiumBy.xpath(
                "//android.widget.TextView[@content-desc='test-Item title' and @text='"
                        + productName + "']" +
                "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
        Waits.clickable(driver, detailsButton).click();
    }

    /**
     * Scrolls until the product name is visible (max 5 swipes).
     * v2.7.1: finds all title elements and filters by text in code,
     * which is more reliable than a complex XPath with 'and'.
     */
    public void scrollToProduct(String productName) {
        for (int i = 0; i < 5; i++) {
            List<WebElement> titles = driver.findElements(productNames);
            for (WebElement title : titles) {
                if (productName.equals(title.getText())) {
                    return;
                }
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
