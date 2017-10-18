package navigator.route

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import navigator.Route
import navigator.route.LocalHistoryRoute.Entry

/**
 * A route that can handle back navigation internally by popping a list.
 *
 * When a [navigator.Navigator] is instructed to pop, the current route is given an opportunity to
 * handle the pop internally. A LocalHistoryRoute handles the pop internally if its list of local
 * history entries is non-empty. Rather than being removed as the current route, the most recent
 * [Entry] is removed from the list and its [Entry.onRemove] is called.
 */
abstract class LocalHistoryRoute<T> : Route<T>() {

    private val localHistory by lazy { mutableListOf<Entry>() }

    override val willHandlePopInternally: Boolean
        get() {
            return localHistory.isNotEmpty()
        }

    override fun willPop(): Deferred<PopDisposition> = async {
        if (willHandlePopInternally) PopDisposition.POP else super.willPop().await()
    }

    override fun didPop(result: Any?): Boolean {
        if (localHistory.isNotEmpty()) {
            val entry = localHistory.removeAt(localHistory.lastIndex)
            assert(entry.owner == this)
            entry.owner = null
            entry.notifyRemoved()
            if (localHistory.isEmpty()) {
                changedInternalState()
            }
            return false
        }
        return super.didPop(result)
    }

    /**
     * Adds a local history entry to this route.
     *
     * When asked to pop, if this route has any local history entries, this route will handle the
     * pop internally by removing the most recently added local history entry.
     *
     * The given local history entry must not already be part of another local history route.
     */
    fun addLocalHistoryEntry(entry: Entry) {
        assert(entry.owner == null)
        entry.owner = this
        val wasEmpty = localHistory.isEmpty()
        localHistory.add(entry)
        if (wasEmpty) {
            changedInternalState()
        }
    }

    /**
     * Remove a local history entry from this route.
     *
     * The entry's [Entry.onRemove] callback, if any, will be called synchronously.
     */
    fun removeLocalHistoryEntry(entry: Entry) {
        assert(entry.owner == this)
        assert(localHistory.contains(entry))
        localHistory.remove(entry)
        entry.owner = null
        entry.notifyRemoved()
        if (localHistory.isEmpty()) {
            changedInternalState()
        }
    }

    /**
     * Called whenever the internal state of the route has changed.
     *
     * This should be called whenever [willHandlePopInternally] and [didPop] might change the value
     * they return. It is used by [ModalRoute], for example, to report the new information via its
     * inherited widget to any children of the route.
     */
    open fun changedInternalState() {}

    /**
     * An entry in the history of a [LocalHistoryRoute].
     */
    class Entry(private val onRemove: () -> Unit) {

        internal var owner: LocalHistoryRoute<*>? = null

        /**
         * Remove this entry from the history of its associated [LocalHistoryRoute].
         */
        fun remove() {
            owner?.removeLocalHistoryEntry(this)
            assert(owner == null)
        }

        internal fun notifyRemoved() {
            onRemove?.invoke()
        }

    }

}
