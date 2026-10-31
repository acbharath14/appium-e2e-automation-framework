package com.bharath.mobile.tests;

import com.bharath.mobile.base.DriverFactory;
import com.bharath.mobile.pages.CartPage;
import com.bharath.mobile.pages.LoginPage;
import com.bharath.mobile.pages.ProductsPage;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * TEMPORARY: dumps iOS page source at key screens for locator analysis.
 * Run with: mvn test -Dtest=IosDumpTest -Dplatform=ios
 * Output goes to target/ios-dumps/.
 */
public class IosDumpTest {

    private static AppiumDriver driver;

    @BeforeAll
    static void setUp() throws Exception {
        driver = DriverFactory.createDriver();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void dump(String name) throws Exception {
        String source = driver.getPageSource();
        Path dir = Paths.get("target", "ios-dumps");
        Files.createDirectories(dir);
        Path file = dir.resolve(name + ".xml");
        Files.writeString(file, source);
        System.out.println("DUMPED: " + file.toAbsolutePath() + " (" + source.length() + " chars)");
    }

    @Test
    void dumpScreens() throws Exception {
        LoginPage login = new LoginPage(driver);
        login.loginAs("standard_user", "secret_sauce");

        ProductsPage products = new ProductsPage(driver);
        dump("01-products");

        // Add an item to get the badge showing
        products.addToCart("Sauce Labs Backpack");
        Thread.sleep(2000);
        dump("02-products-with-badge");

        products.openCart();
        Thread.sleep(2000);
        dump("03-cart");

        // Go back to products for the details hierarchy
        driver.navigate().back();
        Thread.sleep(2000);
        dump("04-products-again");
    }
}
