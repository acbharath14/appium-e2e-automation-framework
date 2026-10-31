package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import com.bharath.mobile.utils.Gestures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gestures and app-state behavior: scrolling and background/foreground.
 */
class GesturesTests extends BaseTest {

    @BeforeEach
    void login() {
        new LoginPage(driver).loginAs("standard_user", "secret_sauce");
    }

    @Test
    @DisplayName("scrolls down and back up without losing the product list")
    void scrollDownAndUpKeepsList() {
        ProductsPage products = new ProductsPage(driver);
        assertTrue(products.isDisplayed());

        Gestures.scrollDown(driver);
        Gestures.scrollUp(driver);

        assertTrue(products.isDisplayed(),
                "expected the product list to still be showing after scrolling");
    }

    @Test
    @DisplayName("keeps the cart after backgrounding and foregrounding the app")
    void backgroundForegroundKeepsCart() {
        ProductsPage products = new ProductsPage(driver);
        products.addToCart("Sauce Labs Backpack");
        assertEquals(1, products.cartBadgeCount());

        // Background the app for 3 seconds, then bring it back. The cart is
        // server/session state in this app, so it must survive.
        ((io.appium.java_client.InteractsWithApps) driver)
                .runAppInBackground(java.time.Duration.ofSeconds(3));

        // After foreground, the UI may need a moment to restore. Wait for the
        // products page to be ready before reading the badge.
        ProductsPage foreground = new ProductsPage(driver);
        assertTrue(foreground.isDisplayed(),
                "expected the products list after foregrounding the app");
        // The badge TextView may lag behind the page render; poll briefly.
        int badge = 0;
        for (int i = 0; i < 5; i++) {
            badge = foreground.cartBadgeCount();
            if (badge == 1) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertEquals(1, badge,
                "expected the cart badge to survive background/foreground");
    }
}
