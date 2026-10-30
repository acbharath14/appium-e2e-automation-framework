package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.CartPage;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cart behavior: add, remove, and the badge count.
 */
class CartTests extends BaseTest {

    @BeforeEach
    void login() {
        new LoginPage(driver).loginAs("standard_user", "secret_sauce");
    }

    @Test
    @DisplayName("adds one product to the cart and the badge shows 1")
    void addOneProductBadgeShowsOne() {
        ProductsPage products = new ProductsPage(driver);
        products.addToCart("Sauce Labs Backpack");

        assertEquals(1, products.cartBadgeCount());
    }

    @Test
    @DisplayName("cart badge count matches the number of added products")
    void badgeMatchesAddedCount() {
        ProductsPage products = new ProductsPage(driver);
        products.addToCart("Sauce Labs Backpack");
        products.addToCart("Sauce Labs Bike Light");

        assertEquals(2, products.cartBadgeCount());
    }

    @Test
    @DisplayName("removes a product from the cart")
    void removeProductFromCart() {
        ProductsPage products = new ProductsPage(driver);
        products.addToCart("Sauce Labs Backpack");
        products.openCart();

        CartPage cart = new CartPage(driver);
        assertTrue(cart.isDisplayed());
        assertEquals(1, cart.itemCount());

        cart.removeFirstItem();
        assertEquals(0, cart.itemCount(), "expected the cart to be empty after removal");
    }
}
