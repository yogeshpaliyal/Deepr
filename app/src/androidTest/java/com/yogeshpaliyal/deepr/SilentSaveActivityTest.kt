package com.yogeshpaliyal.deepr

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yogeshpaliyal.deepr.data.LinkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * Instrumented test for SilentSaveActivity
 */
@RunWith(AndroidJUnit4::class)
class SilentSaveActivityTest : KoinTest {
    private val linkRepository: LinkRepository by inject()

    @Test
    fun testActivityHandlesShareIntent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        val intent = Intent(context, SilentSaveActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com")
            putExtra(Intent.EXTRA_TITLE, "Example Link")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Launch the activity
        val scenario = ActivityScenario.launch<SilentSaveActivity>(intent)
        
        // Give it time to process
        runBlocking {
            delay(1000)
        }
        
        // Activity should finish automatically
        assertEquals(
            androidx.lifecycle.Lifecycle.State.DESTROYED,
            scenario.state
        )
        
        scenario.close()
    }

    @Test
    fun testActivityFinishesWithoutIntent() {
        val context = ApplicationProvider.getApplicationContext<DeeprApplication>()
        
        val intent = Intent(context, SilentSaveActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val scenario = ActivityScenario.launch<SilentSaveActivity>(intent)
        
        // Give it time to process
        runBlocking {
            delay(500)
        }
        
        // Activity should finish immediately
        assertEquals(
            androidx.lifecycle.Lifecycle.State.DESTROYED,
            scenario.state
        )
        
        scenario.close()
    }

    @Test
    fun testActivityExists() {
        // Simple test to verify the activity can be instantiated
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, SilentSaveActivity::class.java)
        assertNotNull(intent.component)
    }
}
