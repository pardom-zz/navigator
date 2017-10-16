package navigator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import navigator.Overlay.Entry
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [FrameLayout] of entries that can be managed independently.
 *
 * Overlays let independent child widgets "float" visual elements on top of other widgets by
 * inserting them into the overlay's [FrameLayout]. The overlay lets each of these widgets manage
 * their participation in the overlay using [Entry] objects.
 *
 * Although you can implement an [Overlay] directly, it's most common to use the overlay implemented
 * by [Navigator]. The navigator uses its overlay to manage the visual appearance of its routes.
 *
 * @see [Entry].
 */
abstract class Overlay : FrameLayout {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract properties

    /**
     * The entries to include in the overlay initially.
     *
     * These entries are only used when the [Overlay] is inflated.
     *
     * To add entries to an [Overlay] that is already in the tree, use [Overlay.of] to obtain the
     * [Overlay], and then use [Overlay.insert] or [Overlay.insertAll].
     *
     * To remove an entry from an [Overlay], use [Entry.remove].
     */
    abstract val initialEntries: Collection<Entry>

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Private properties

    private val _entries = mutableListOf<Entry>()
    private val initialized = AtomicBoolean(false)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors

    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0,
            defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle overrides

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (initialized.compareAndSet(false, true)) {
            insertAll(initialEntries)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public API

    /**
     * Insert the given entry into the overlay.
     *
     * If [above] is non-null, the entry is inserted just above [above]. Otherwise, the entry is
     * inserted on top.
     */
    fun insert(entry: Entry, above: Entry? = null) {
        require(entry._overlay == null)
        require(above == null || (above._overlay == this && _entries.contains(above)))
        entry._overlay = this
        val index = if (above == null) _entries.size else _entries.indexOf(above) + 1
        _entries.add(index, entry)
        val view = entry.builder(context)
        addView(view, index)
        updateChildren()
    }

    /**
     * Insert all the entries in the given collection.
     *
     * If [above] is non-null, the entries are inserted just above [above]. Otherwise, the entries
     * are inserted on top.
     */
    fun insertAll(entries: Collection<Entry>, above: Entry? = null) {
        require(above == null || (above._overlay == this && _entries.contains(above)))
        if (entries.isEmpty()) return
        val index = if (above == null) _entries.size else _entries.indexOf(above) + 1
        _entries.addAll(index, entries)
        entries.forEachIndexed { i, entry ->
            require(entry._overlay == null)
            entry._overlay = this
            val view = entry.builder(context)
            addView(view, index + i)
        }
        updateChildren()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Private functions

    private fun remove(entry: Entry) {
        val index = _entries.indexOf(entry)
        _entries.removeAt(index)
        removeViewAt(index)
        updateChildren()
    }

    private fun updateChildren() {
        var onstage = true
        val count = _entries.size
        _entries.reversed().forEachIndexed { index, entry ->
            val view = getChildAt(count - index - 1)
            view.visibility = if (onstage) View.VISIBLE else View.GONE
            if (entry.opaque) {
                onstage = false
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Classes

    /**
     * A place in an [Overlay] that can contain a widget.
     *
     * Overlay entries are inserted into an [Overlay] using the [Overlay.insert] or
     * [Overlay.insertAll] functions. To find the closest enclosing overlay for a given [View], use
     * the [Overlay.of] function.
     *
     * An overlay entry can be in at most one overlay at a time. To remove an entry from its
     * overlay, call the [remove] function on the overlay entry.
     *
     * @see [Overlay].
     */
    class Entry(builder: ViewBuilder, opaque: Boolean) {

        internal var _overlay: Overlay? = null

        /**
         * This entry will include the widget built by this builder in the overlay at the entry's
         * position.
         */
        val builder: ViewBuilder = builder

        /**
         * Whether this entry occludes the entire overlay.
         */
        var opaque: Boolean = opaque
            set(value) {
                if (field == value) return
                field = value
                require(_overlay != null)
                _overlay!!.updateChildren()
            }

        /**
         * Remove this entry from the overlay.
         *
         * This should only be called once.
         */
        fun remove() {
            require(_overlay != null)
            val overlay = _overlay
            _overlay = null
            overlay!!.remove(this)
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Companion

    companion object {

        /**
         * The state from the closest instance of this class that encloses the given view.
         *
         * Typical usage is as follows:
         *
         * ```
         * val overlay = Overlay.of(view)
         * ```
         */
        tailrec fun of(view: View): Overlay {
            tailrec fun of(viewParent: ViewParent?): Overlay {
                if (viewParent is Overlay) return viewParent
                if (viewParent == null) error("Overlay not an ancestor of $view.")
                return of(viewParent.parent)
            }
            if (view is Overlay) return view
            return of(view.parent)
        }

    }

}
