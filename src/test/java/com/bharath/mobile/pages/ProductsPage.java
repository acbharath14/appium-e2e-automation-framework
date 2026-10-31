package com.bharath.mobile.pages;

import com.bharath.mobile.utils.Gestures;
import com.bharath.mobile.utils.Platform;
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
     * v2.7.1 Android: the count is in a child TextView of the test-Cart
     * container, not in the container's own text (which is always empty).
     * v2.7.1 iOS: the count is in the label attribute of test-Cart itself
     * (e.g. label="1"); the badge is a child XCUIElementTypeOther.
     */
    public int cartBadgeCount() {
        List<WebElement> badges = driver.findElements(cartBadge);
        if (badges.isEmpty()) {
            return 0;
        }
        WebElement badge = badges.get(0);
        if (Platform.isIOS()) {
            String label = badge.getAttribute("label");
            if (label == null || !label.trim().matches("\\d+")) {
                return 0;
            }
            return Integer.parseInt(label.trim());
        }
        List<WebElement> countElements =
                badge.findElements(AppiumBy.xpath(".//" + Platform.textElement()));
        if (countElements.isEmpty()) {
            return 0;
        }
        String text = countElements.get(0).getText();
        return text.isEmpty() ? 0 : Integer.parseInt(text.trim());
    }

    /**
     * Opens the cart.
     * v2.7.1 Android: the test-Cart container is not clickable and has no
     * clickable ancestor/descendant with a stable locator; the tap target is
     * the overlapping clickable ViewGroup at the same bounds. Taps the center
     * of the test-Cart bounds via W3C Actions.
     * v2.7.1 iOS: the cart button has accessible=false and XCUITest cannot tap
     * it (7 methods tried). The app registers the swaglabs:// URL scheme with
     * a 'cart' path (React Navigation deep link, verified in main.jsbundle),
     * so navigate via mobile: deepLink instead.
     */
    public void openCart() {
        WebElement cart = Waits.visible(driver, cartBadge);
        if (Platform.isIOS()) {
            // Deep link to cart: bypasses the non-accessible cart button
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("url", "swaglabs://cart");
            params.put("bundleId", "com.saucelabs.SwagLabsMobileApp");
            driver.executeScript("mobile: deepLink", params);
            return;
        }
        org.openqa.selenium.Rectangle rect = cart.getRect();
        int centerX = rect.getX() + rect.getWidth() / 2;
        int centerY = rect.getY() + rect.getHeight() / 2;
        Gestures.tapAt(driver, centerX, centerY);
    }

    /**
     * Opens the product details screen. v2.7.1: there is no
     * test-Item-&lt;name&gt; id; the tap target is the clickable ancestor
     * ViewGroup of the product title TextView.
     */
    public void openProductDetails(String productName) {
        scrollToProduct(productName);
        // Find the tappable row container in ONE atomic lookup: the clickable
        // ViewGroup containing this product's title. The previous approach —
        // find the title, then walk its ancestor:: axis — failed repeatedly
        // even though the dump shows the clickable parent exists: across
        // React Native re-renders the title node the driver holds and the
        // tree the ancestor query evaluates can disagree. A single predicate
        // sees one consistent tree, so the two can never skew.
        By rowContainer;
        if (Platform.isIOS()) {
            // iOS product-details navigation is skipped in tests (XCUITest
            // lacks ancestor::); this branch is structural parity only.
            rowContainer = AppiumBy.xpath("//" + Platform.container()
                    + "[.//*[@name='test-Item title' and @label='"
                    + productName + "']]");
        } else {
            rowContainer = AppiumBy.xpath("//" + Platform.container()
                    + "[@clickable='true']"
                    + "[.//*[@content-desc='test-Item title' and @text='"
                    + productName + "']]");
        }
        try {
            Waits.clickable(driver, rowContainer).click();
        } catch (org.openqa.selenium.TimeoutException e) {
            // Dump the tree so a failure carries its own evidence.
            // Absolute path: the forked test JVM's working directory is not
            // guaranteed to be the repo root, so never rely on a relative one.
            String dumpPath = System.getProperty("user.dir")
                    + "/target/product-details-timeout.xml";
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of(dumpPath), driver.getPageSource());
                System.out.println("PRODUCT-DETAILS DUMP WRITTEN TO: " + dumpPath);
            } catch (Exception io) {
                dumpPath = "dump failed: " + io.getMessage();
            }
            throw new org.openqa.selenium.TimeoutException(
                    "product row container for '" + productName
                            + "' never appeared; page source at " + dumpPath, e);
        }
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
