package navigator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout

abstract class Overlay : FrameLayout {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract properties

    abstract val initialEntries: List<Entry>

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Private properties

    private val _entries = mutableListOf<Entry>()

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
    // Public API

    /**
     * Insert the given entry into the overlay.
     *
     * If [above] is non-null, the entry is inserted just above [above].
     * Otherwise, the entry is inserted on top.
     */
    fun insert(entry: Entry, above: Entry? = null) {
        assert(entry._overlay == null)
        assert(above == null || (above._overlay == this && _entries.contains(above)))
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
     * If [above] is non-null, the entries are inserted just above [above].
     * Otherwise, the entries are inserted on top.
     */
    fun insertAll(entries: Collection<Entry>, above: Entry? = null) {
        assert(above == null || (above._overlay == this && _entries.contains(above)))
        if (entries.isEmpty()) return
        val index = if (above == null) _entries.size else _entries.indexOf(above) + 1
        _entries.addAll(index, entries)
        entries.forEachIndexed { i, entry ->
            assert(entry._overlay == null)
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

    data class Entry(
            val builder: (Context) -> View,
            val opaque: Boolean,
            val maintainState: Boolean) {

        internal var _overlay: Overlay? = null

        fun remove() {
            assert(_overlay != null)
            val overlay = _overlay
            _overlay = null
            overlay!!.remove(this)
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Companion

    companion object {

        tailrec fun of(view: View): Overlay {
            tailrec fun of(viewParent: ViewParent?): Overlay {
                if (viewParent is Overlay) return viewParent
                if (viewParent == null) throw IllegalArgumentException("Overlay not an ancestor of $view.")
                return of(viewParent.parent)
            }
            if (view is Overlay) return view
            return of(view.parent)
        }

    }

}
