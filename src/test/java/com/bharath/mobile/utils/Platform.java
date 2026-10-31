package com.bharath.mobile.utils;

/**
 * Platform detection and platform-aware locator helpers.
 *
 * <p>The suite runs on Android (default) and iOS via
 * {@code -Dplatform=ios}. The Swag Labs app exposes the same
 * {@code test-} accessibility ids on both (via {@code accessibilityLabel}
 * on Android, {@code testID} on iOS), so {@code AppiumBy.accessibilityId}
 * locators need no changes. But the native element class names differ, so
 * any XPath that names a class must go through here.
 *
 * <p>v2.7.1 iOS hierarchy is UNVERIFIED (no Xcode on the dev machine yet).
 * The iOS class names below are the standard XCUITest mappings; confirm
 * via a page-source dump on the first iOS run.
 */
public final class Platform {

    private Platform() {
        // utility class
    }

    /** True when running with {@code -Dplatform=ios}. */
    public static boolean isIOS() {
        return "ios".equalsIgnoreCase(System.getProperty("platform", "android"));
    }

    /**
     * XPath node name for a text element:
     * {@code android.widget.TextView} on Android,
     * {@code XCUIElementTypeStaticText} on iOS.
     */
    public static String textElement() {
        return isIOS() ? "XCUIElementTypeStaticText" : "android.widget.TextView";
    }

    /**
     * XPath node name for a generic container:
     * {@code android.view.ViewGroup} on Android,
     * {@code XCUIElementTypeOther} on iOS.
     */
    public static String container() {
        return isIOS() ? "XCUIElementTypeOther" : "android.view.ViewGroup";
    }
}
