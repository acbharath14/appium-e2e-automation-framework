package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.CartPage;
import com.bharath.mobile.pages.CheckoutCompletePage;
import com.bharath.mobile.pages.CheckoutInfoPage;
import com.bharath.mobile.pages.CheckoutOverviewPage;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import com.bharath.mobile.utils.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checkout behavior: form validation, overview totals, completion, cancel.
 */
class CheckoutTests extends BaseTest {

    @BeforeEach
    void loginAndAddItem() {
        // iOS: cart button has accessible=false, XCUITest cannot tap it.
        Assumptions.assumeFalse(Platform.isIOS(),
                "iOS cart navigation not automatable (button accessible=false)");
        new LoginPage(driver).loginAs("standard_user", "secret_sauce");
        ProductsPage products = new ProductsPage(driver);
        products.addToCart("Sauce Labs Backpack");
        products.openCart();
        new CartPage(driver).checkout();
    }

    private void fillValidForm(CheckoutInfoPage info) {
        info.fillForm("Bharath", "Test", "20171");
        info.continueCheckout();
    }

    @Test
    @DisplayName("refuses to continue when first name, last name, or postal code is missing")
    void emptyFormShowsError() {
        CheckoutInfoPage info = new CheckoutInfoPage(driver);
        assertTrue(info.isDisplayed());
        info.continueCheckout();

        assertTrue(info.errorText().contains("First Name is required"),
                "expected a first-name error, got: " + info.errorText());
    }

    @Test
    @DisplayName("shows item total, tax, and total on the order overview")
    void overviewShowsTotals() {
        CheckoutInfoPage info = new CheckoutInfoPage(driver);
        fillValidForm(info);

        CheckoutOverviewPage overview = new CheckoutOverviewPage(driver);
        assertTrue(overview.isDisplayed());
        assertTrue(overview.itemTotalText().startsWith("Item total: $"));
        assertTrue(overview.taxText().startsWith("Tax: $"));
        assertTrue(overview.totalText().startsWith("Total: $"));
    }

    @Test
    @DisplayName("completes checkout and shows the thank-you screen")
    void completeCheckoutShowsConfirmation() {
        CheckoutInfoPage info = new CheckoutInfoPage(driver);
        fillValidForm(info);

        CheckoutOverviewPage overview = new CheckoutOverviewPage(driver);
        overview.finish();

        assertTrue(new CheckoutCompletePage(driver).isDisplayed(),
                "expected the order confirmation screen");
    }

    @Test
    @DisplayName("canceling from the overview returns to the products list")
    void cancelReturnsToProducts() {
        CheckoutInfoPage info = new CheckoutInfoPage(driver);
        fillValidForm(info);

        CheckoutOverviewPage overview = new CheckoutOverviewPage(driver);
        overview.cancel();

        assertTrue(new ProductsPage(driver).isDisplayed(),
                "expected to be back on the products list after cancel");
    }
}
