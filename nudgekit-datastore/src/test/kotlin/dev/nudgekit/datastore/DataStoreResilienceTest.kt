package dev.nudgekit.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

/**
 * Verifies that [DataStoreTipManager] degrades gracefully when the underlying
 * DataStore surfaces a read error, instead of crashing the host app.
 */
class DataStoreResilienceTest {

    /** A DataStore whose read flow always fails with [error]. */
    private class FailingDataStore(private val error: Throwable) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }
        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = throw error
    }

    @Test
    fun `getTipState returns defaults when reads throw IOException`() = runTest {
        val manager = DataStoreTipManager(FailingDataStore(IOException("disk gone")))

        val state = manager.getTipState("tip1")

        assertThat(state.tipId).isEqualTo("tip1")
        assertThat(state.isDismissed).isFalse()
        assertThat(state.displayCount).isEqualTo(0)
        assertThat(state.lastShownAtMillis).isNull()
    }

    @Test
    fun `getCounters returns empty when reads throw IOException`() = runTest {
        val manager = DataStoreTipManager(FailingDataStore(IOException()))

        val counters = manager.getCounters()

        assertThat(counters.eventCounts).isEmpty()
        assertThat(counters.screenVisitCounts).isEmpty()
    }

    @Test
    fun `observeTipState emits a default instead of throwing on IOException`() = runTest {
        val manager = DataStoreTipManager(FailingDataStore(IOException()))

        manager.observeTipState("tip1").test {
            val item = awaitItem()
            assertThat(item.isDismissed).isFalse()
            assertThat(item.displayCount).isEqualTo(0)
            awaitComplete()
        }
    }

    @Test
    fun `observeCounters emits empty instead of throwing on IOException`() = runTest {
        val manager = DataStoreTipManager(FailingDataStore(IOException()))

        manager.observeCounters().test {
            val item = awaitItem()
            assertThat(item.eventCounts).isEmpty()
            assertThat(item.screenVisitCounts).isEmpty()
            awaitComplete()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `non-IO errors are not swallowed`() = runTest {
        // A programming error (not IOException) must propagate, not be masked.
        val manager = DataStoreTipManager(FailingDataStore(IllegalStateException("bug")))
        manager.getTipState("tip1")
    }
}
