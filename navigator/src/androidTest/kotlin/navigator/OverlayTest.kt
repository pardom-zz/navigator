package navigator

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import navigator.Overlay.Entry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class OverlayTest {

    private lateinit var overlay: Overlay

    @Before
    fun setUp() {
        overlay = MockOverlay(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun testNoInitialEntries() {
        MockOverlay(
                InstrumentationRegistry.getTargetContext(),
                emptyList(),
                { view -> assertThat(view.childCount).isEqualTo(0) }
        )
    }

    @Test
    fun testInitialEntries() {
        MockOverlay(
                InstrumentationRegistry.getTargetContext(),
                listOf(
                        createEntry(),
                        createEntry(),
                        createEntry()
                ),
                { view -> assertThat(view.childCount).isEqualTo(3) }
        )
    }

    @Test
    fun testInsertAddsView() {
        overlay.insert(createEntry())
        assertThat(overlay.childCount).isEqualTo(1)
    }

    @Test
    fun testInsertAddsViewAbove() {
        val entry1 = createEntry(1)
        val entry2 = createEntry(2)
        val entry3 = createEntry(3)
        overlay.insert(entry1)
        overlay.insert(entry2)
        overlay.insert(entry3, entry1)
        assertThat(overlay.getChildAt(0).id).isEqualTo(1)
        assertThat(overlay.getChildAt(1).id).isEqualTo(3)
        assertThat(overlay.getChildAt(2).id).isEqualTo(2)
    }

    @Test
    fun testInsertAllAddsViews() {
        overlay.insert(createEntry())
        overlay.insert(createEntry())
        assertThat(overlay.childCount).isEqualTo(2)
    }

    @Test
    fun testInsertAllAddsViewsAbove() {
        val entry1 = createEntry(1)
        val entry2 = createEntry(2)
        val entry3 = createEntry(3)
        val entry4 = createEntry(4)
        overlay.insert(entry1)
        overlay.insert(entry2)
        overlay.insertAll(listOf(entry3, entry4), entry1)
        assertThat(overlay.getChildAt(0).id).isEqualTo(1)
        assertThat(overlay.getChildAt(1).id).isEqualTo(3)
        assertThat(overlay.getChildAt(2).id).isEqualTo(4)
        assertThat(overlay.getChildAt(3).id).isEqualTo(2)
    }

    @Test
    fun testRemoveRemovesView() {
        val entry = createEntry()
        overlay.insert(entry)
        entry.remove()
        assertThat(overlay.childCount).isEqualTo(0)
    }

    @Test
    fun testOpaqueView() {
        overlay.insert(createEntry())
        overlay.insert(createEntry())
        assertThat(overlay.getChildAt(0).visibility).isEqualTo(View.GONE)
        assertThat(overlay.getChildAt(1).visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun testNonOpaqueView() {
        overlay.insert(createEntry())
        overlay.insert(createEntry(opaque = false))
        assertThat(overlay.getChildAt(0).visibility).isEqualTo(View.VISIBLE)
        assertThat(overlay.getChildAt(1).visibility).isEqualTo(View.VISIBLE)
    }

    private class MockOverlay(
            context: Context,
            override val initialEntries: Collection<Entry> = emptyList(),
            private val finishInflateListener: (ViewGroup) -> Unit = {}) : Overlay(context) {

        override fun onFinishInflate() {
            super.onFinishInflate()
            finishInflateListener(this)
        }

    }

    private fun createEntry(id: Int = 0, opaque: Boolean = true) = Entry(
            { context -> TextView(context).apply { setId(id) } },
            opaque
    )

}
