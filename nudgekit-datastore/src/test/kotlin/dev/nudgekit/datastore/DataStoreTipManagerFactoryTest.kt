package dev.nudgekit.datastore

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies [DataStoreTipManager.create] is safe to call repeatedly — it returns
 * the same process-wide instance rather than constructing a second DataStore
 * over the same file (which AndroidX DataStore forbids at runtime).
 *
 * Uses Robolectric so a real [android.content.Context] is available on the JVM.
 */
@RunWith(AndroidJUnit4::class)
class DataStoreTipManagerFactoryTest {

    @Test
    fun `create returns the same cached instance across calls`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val first = DataStoreTipManager.create(context)
        val second = DataStoreTipManager.create(context)

        assertThat(second).isSameInstanceAs(first)
    }

    @Test
    fun `create is stable when called with different context references`() {
        // applicationContext is shared, so the cache key (file name) is identical
        // even if callers pass different Context wrappers.
        val app = ApplicationProvider.getApplicationContext<android.content.Context>()

        val first = DataStoreTipManager.create(app)
        val second = DataStoreTipManager.create(app.applicationContext)

        assertThat(second).isSameInstanceAs(first)
    }
}
