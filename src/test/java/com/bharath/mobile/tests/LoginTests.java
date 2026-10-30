package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Login behavior: the happy path and the two credential failures.
 *
 * <p>Swag Labs credentials mirror the saucedemo web app: standard_user /
 * secret_sauce is valid, locked_out_user is rejected.
 */
class LoginTests extends BaseTest {

    @Test
    @DisplayName("logs in with valid credentials and lands on the products list")
    void validLoginLandsOnProducts() {
        LoginPage login = new LoginPage(driver);
        assertTrue(login.isDisplayed(), "expected the login screen on a fresh launch");

        login.loginAs("standard_user", "secret_sauce");

        assertTrue(new ProductsPage(driver).isDisplayed(),
                "expected the products list after a valid login");
    }

    @Test
    @DisplayName("shows an error message for a wrong password")
    void wrongPasswordShowsError() {
        LoginPage login = new LoginPage(driver);
        login.loginAs("standard_user", "not-the-password");

        assertEquals("Username and password do not match any user in this service.",
                login.errorText());
    }

    @Test
    @DisplayName("shows an error message for the locked-out user")
    void lockedOutUserShowsError() {
        LoginPage login = new LoginPage(driver);
        login.loginAs("locked_out_user", "secret_sauce");

        assertEquals("Sorry, this user has been locked out.", login.errorText());
    }
}
