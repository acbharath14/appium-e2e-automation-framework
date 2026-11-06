package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.CartPage;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import com.bharath.mobile.utils.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cart behavior: add, remove, and the badge count.
 */
@Epic("Swag Labs Mobile")
@Feature("Cart")
class CartTests extends BaseTest {

    @BeforeEach
    void login() {
        new LoginPage(driver).loginAs("standard_user", "secret_sauce");
    }

    @ParameterizedTest(name = "adding {0} product(s) shows a badge of {1}")
    @CsvSource({"1, 1", "2, 2"})
    @DisplayName("cart badge count matches the number of added products")
    void cartBadgeReflectsAddedCount(int productCount, int expectedBadge) {
        ProductsPage products = new ProductsPage(driver);
        String[] names = {"Sauce Labs Backpack", "Sauce Labs Bike Light"};
        for (int i = 0; i < productCount; i++) {
            products.addToCart(names[i]);
        }

        assertEquals(expectedBadge, products.cartBadgeCount());
    }

    @Test
    @DisplayName("removes a product from the cart")
    void removeProductFromCart() {
        // iOS: cart button has accessible=false, XCUITest cannot tap it.
        // Deep link swaglabs://cart does not navigate (param format unknown).
        Assumptions.assumeFalse(Platform.isIOS(),
                "iOS cart navigation not automatable (button accessible=false)");
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
