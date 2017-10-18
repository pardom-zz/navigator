package navigator.route

import navigator.Overlay
import navigator.Route

/**
 * A route that displays widgets in the [Navigator]'s [Overlay].
 */
abstract class OverlayRoute<T> : Route<T>() {

    /**
     * Subclasses should override this getter to return the builders for the overlay.
     */
    abstract fun createOverlayEntries(): Collection<Overlay.Entry>

    /**
     * Controls whether [didPop] calls [Navigator.finalizeRoute].
     *
     * If true, this route removes its overlay entries during [didPop]. Subclasses can override this
     * getter if they want to delay finalization (for example to animate the route's exit before
     * removing it from the overlay).
     *
     * Subclasses that return false from [finishedWhenPopped] are responsible for calling
     * [Navigator.finalizeRoute] themselves.
     */
    open val finishedWhenPopped: Boolean = true

    override fun install(insertionPoint: Overlay.Entry?) {
        assert(overlayEntries.isEmpty())
        navigator?.insertAll(createOverlayEntries(), insertionPoint)
        super.install(insertionPoint)
    }

    override fun didPop(result: Any?): Boolean {
        val returnValue = super.didPop(result)
        assert(returnValue)
        if (finishedWhenPopped) {
            navigator?.finalizeRoute(this)
        }
        return returnValue
    }

    override fun dispose() {
        overlayEntries.forEach { entry ->
            entry.remove()
        }
        overlayEntries.clear()
        super.dispose()
    }

}
