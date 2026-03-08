package dev.cosgy.textToSpeak.queue

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FairQueueTest {
    private data class Item(override val identifier: Long, val name: String) : Queueable

    @Test
    fun add_placesItemAfterCurrentFairWindow() {
        val queue = FairQueue<Item>()
        queue.add(Item(1, "a1"))
        queue.add(Item(2, "b1"))
        queue.add(Item(3, "c1"))
        queue.add(Item(2, "b2"))

        val insertIndex = queue.add(Item(1, "a2"))

        assertEquals(3, insertIndex)
        assertEquals(listOf(1L, 2L, 3L, 1L, 2L), queue.getList().map { it.identifier })
    }

    @Test
    fun removeAll_deletesOnlyMatchingIdentifier() {
        val queue = FairQueue<Item>()
        queue.addAt(0, Item(10, "x1"))
        queue.addAt(1, Item(20, "y1"))
        queue.addAt(2, Item(10, "x2"))
        queue.addAt(3, Item(30, "z1"))
        queue.addAt(4, Item(10, "x3"))

        val removed = queue.removeAll(10)

        assertEquals(3, removed)
        assertEquals(listOf(20L, 30L), queue.getList().map { it.identifier })
    }

    @Test
    fun addAt_withOutOfRangeIndex_appendsToTail() {
        val queue = FairQueue<Item>()

        queue.addAt(100, Item(1, "tail"))

        assertEquals(1, queue.size())
        assertEquals(1L, queue[0].identifier)
    }

    @Test
    fun pull_returnsHeadAndUpdatesEmptyState() {
        val queue = FairQueue<Item>()
        queue.addAt(0, Item(1, "first"))
        queue.addAt(1, Item(2, "second"))

        assertFalse(queue.isEmpty)
        assertEquals("first", queue.pull().name)
        assertEquals(1, queue.size())
        assertEquals("second", queue.pull().name)
        assertTrue(queue.isEmpty)
    }
}
