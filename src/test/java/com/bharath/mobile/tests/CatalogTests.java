package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductDetailsPage;
import com.bharath.mobile.pages.ProductsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Catalog behavior: listing, details, and scrolling.
 */
class CatalogTests extends BaseTest {

    @BeforeEach
    void login() {
        new LoginPage(driver).loginAs("standard_user", "secret_sauce");
    }

    @Test
    @DisplayName("lists every product with a name and a price")
    void productsHaveNamesAndPrices() {
        ProductsPage products = new ProductsPage(driver);
        assertTrue(products.isDisplayed());
        assertTrue(products.visibleProductCount() > 0,
                "expected at least one product on screen");
    }

    @Test
    @DisplayName("opens the product details screen when a product is tapped")
    void tappingProductOpensDetails() {
        ProductsPage products = new ProductsPage(driver);
        products.openProductDetails("Sauce Labs Backpack");

        ProductDetailsPage details = new ProductDetailsPage(driver);
        assertTrue(details.isDisplayed());
        assertEquals("Sauce Labs Backpack", details.name());
        assertTrue(details.price().startsWith("$"), "expected a dollar price");
    }

    @Test
    @DisplayName("scrolls the product list to reveal off-screen products")
    void scrollRevealsMoreProducts() {
        ProductsPage products = new ProductsPage(driver);
        // "Sauce Labs Onesie" sits below the fold on a phone screen: reaching
        // it proves the scroll-until-visible pattern works.
        products.scrollToProduct("Sauce Labs Onesie");
        assertTrue(products.visibleProductCount() > 0);
    }
}
