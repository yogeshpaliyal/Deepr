package com.yogeshpaliyal.deepr.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun newerReleaseTagIsDetected() {
        assertTrue(isNewerVersion("v1.0.29", "1.0.28"))
        assertTrue(isNewerVersion("v1.1.0", "1.0.28"))
        assertTrue(isNewerVersion("v2.0", "1.9.9"))
        assertTrue(isNewerVersion("1.0.10", "1.0.9"))
    }

    @Test
    fun sameOrOlderReleaseIsNotAnUpdate() {
        assertFalse(isNewerVersion("v1.0.28", "1.0.28"))
        assertFalse(isNewerVersion("v1.0.27", "1.0.28"))
        assertFalse(isNewerVersion("v1.0", "1.0.0"))
    }

    @Test
    fun buildSuffixesAreIgnored() {
        assertFalse(isNewerVersion("v1.0.28", "1.0.28-debug"))
        assertTrue(isNewerVersion("v1.0.29", "1.0.28-debug"))
    }

    @Test
    fun invalidTagIsNotAnUpdate() {
        assertFalse(isNewerVersion("nightly", "1.0.28"))
        assertFalse(isNewerVersion("", "1.0.28"))
    }
}
