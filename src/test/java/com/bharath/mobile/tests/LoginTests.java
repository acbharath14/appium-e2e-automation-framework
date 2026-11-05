package com.bharath.mobile.tests;

import com.bharath.mobile.base.BaseTest;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Login behavior: the happy path and the credential failures.
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

    @ParameterizedTest(name = "rejected: {0} / {1}")
    @CsvSource({
        "standard_user, not-the-password, Username and password do not match any user in this service.",
        "locked_out_user, secret_sauce, 'Sorry, this user has been locked out.'"
    })
    @DisplayName("shows the exact error message for rejected credentials")
    void invalidCredentialsShowError(String username, String password, String expectedMessage) {
        LoginPage login = new LoginPage(driver);
        login.loginAs(username, password);

        assertEquals(expectedMessage, login.errorText());
    }
}
