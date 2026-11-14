package com.yogeshpaliyal.deepr

import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test for utility functions, specifically link normalization
 */
class UtilsTest {
    @Test
    fun normalizeLink_addsHttpsToUrlWithoutScheme() {
        assertEquals("https://cnn.com", normalizeLink("cnn.com"))
        assertEquals("https://www.google.com", normalizeLink("www.google.com"))
        assertEquals("https://example.com/path", normalizeLink("example.com/path"))
    }

    @Test
    fun normalizeLink_preservesExistingHttpsScheme() {
        assertEquals("https://cnn.com", normalizeLink("https://cnn.com"))
        assertEquals("https://www.google.com", normalizeLink("https://www.google.com"))
    }

    @Test
    fun normalizeLink_preservesExistingHttpScheme() {
        assertEquals("http://example.com", normalizeLink("http://example.com"))
    }

    @Test
    fun normalizeLink_preservesCustomSchemes() {
        assertEquals("app://deeplink", normalizeLink("app://deeplink"))
        assertEquals("myapp://open", normalizeLink("myapp://open"))
        assertEquals("intent://action", normalizeLink("intent://action"))
    }

    @Test
    fun normalizeLink_handlesBlankInput() {
        assertEquals("", normalizeLink(""))
        assertEquals("", normalizeLink("   "))
    }

    @Test
    fun normalizeLink_trimsWhitespace() {
        assertEquals("https://cnn.com", normalizeLink("  cnn.com  "))
        assertEquals("https://example.com", normalizeLink(" example.com "))
    }

    @Test
    fun normalizeLink_handlesUrlsWithoutDots() {
        // For custom schemes without dots, return as-is
        assertEquals("localhost", normalizeLink("localhost"))
    }

    @Test
    fun normalizeLink_handlesComplexUrls() {
        assertEquals(
            "https://www.example.com/path?query=value",
            normalizeLink("www.example.com/path?query=value"),
        )
        assertEquals(
            "https://example.com:8080/path",
            normalizeLink("example.com:8080/path"),
        )
    }

    @Test
    fun isValidDeeplink_acceptsHierarchicalLinks() {
        assertTrue(isValidDeeplink("https://example.com"))
        assertTrue(isValidDeeplink("example.com/path"))
        assertTrue(isValidDeeplink("app://deeplink"))
        assertTrue(isValidDeeplink("intent://scan/#Intent;scheme=zxing;end"))
    }

    @Test
    fun isValidDeeplink_acceptsOpaqueLinks() {
        assertTrue(isValidDeeplink("about:config"))
        assertTrue(isValidDeeplink("about:config?filter=browser.tabs"))
        assertTrue(isValidDeeplink("mailto:someone@example.com"))
        assertTrue(isValidDeeplink("tel:+911234567890"))
    }

    @Test
    fun isValidDeeplink_rejectsInvalidLinks() {
        assertFalse(isValidDeeplink(""))
        assertFalse(isValidDeeplink("   "))
        assertFalse(isValidDeeplink("localhost"))
        assertFalse(isValidDeeplink("localhost:8080"))
        assertFalse(isValidDeeplink("hello world"))
        assertFalse(isValidDeeplink("https://"))
        assertFalse(isValidDeeplink("about:"))
        assertFalse(isValidDeeplink("1abc:foo"))
    }
}
