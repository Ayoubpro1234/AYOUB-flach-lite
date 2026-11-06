package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.repository.FlashlightRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

class FlashlightRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUserCannotWriteSettings() = runBlocking {
    auth.signOut()
    val repository = FlashlightRepository(firestore)
    try {
      repository.saveUserSettings("NORMAL", 5, true)
      fail("Expected IllegalStateException for unauthenticated caller")
    } catch (e: IllegalStateException) {
      // Expected
    }
  }

  @Test
  fun authenticatedUserCanSaveAndRetrieveSettings() = runBlocking {
    val uid = signInTestUser("flashlight_user@example.com")
    val repository = FlashlightRepository(firestore)

    repository.saveUserSettings(
      defaultMode = "STROBE",
      strobeFrequency = 8,
      hapticFeedback = true,
      screenLightColor = "#FFEB3B",
      screenBrightness = 0.8
    )

    val settings = repository.observeUserSettings().first()
    assertNotNull(settings)
    assertEquals(uid, settings?.userId)
    assertEquals("STROBE", settings?.defaultMode)
    assertEquals(8, settings?.strobeFrequency)
    assertEquals(true, settings?.hapticFeedback)
    assertEquals("#FFEB3B", settings?.screenLightColor)
  }

  @Test
  fun authenticatedUserCanAddAndObserveLogs() = runBlocking {
    val uid = signInTestUser("logger_${System.currentTimeMillis()}@example.com")
    val repository = FlashlightRepository(firestore)

    repository.addLog("ON", 12)
    repository.addLog("SOS", 45)

    val logs = repository.observeRecentLogs(5).filter { it.size >= 2 }.first()
    assertEquals(2, logs.size)
    assertEquals(uid, logs[0].userId)
  }
}
