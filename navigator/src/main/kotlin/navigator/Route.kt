package navigator

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

/**
 * An abstraction for an entry managed by a [Navigator].
 *
 * This class defines an abstract interface between the navigator and the "routes" that are pushed
 * on and popped off the navigator. Most routes have visual affordances, which they place in the
 * navigators [Overlay] using one or more [Overlay.Entry] objects.
 *
 * See [Navigator] for more explanation of how to use a Route with navigation, including code
 * examples.
 */
abstract class Route<T> {

    /**
     * The navigator that the route is in, if any.
     */
    var navigator: Navigator? = null

    /**
     * The overlay entries for this route.
     */
    val overlayEntries: MutableList<Overlay.Entry> = mutableListOf()

    /**
     * A future that completes when this route is popped off the navigator.
     *
     * The future completes with the value given to [Navigator.pop], if any.
     */
    val popped: CompletableDeferred<Any?> = CompletableDeferred()

    /**
     * When this route is popped (see [Navigator.pop]) if the result isn't specified or if it's
     * null, this value will be used instead.
     */
    open val currentResult: T? = null

    /**
     * Whether calling [didPop] would return false.
     */
    open val willHandlePopInternally: Boolean = false

    /**
     * Whether this route is the top-most route on the navigator.
     *
     * If this is true, then [isActive] is also true.
     */
    val isCurrent: Boolean
        get() {
            return navigator?.history?.last() == this
        }

    /**
     * Whether this route is the bottom-most route on the navigator.
     *
     * If this is true, then [Navigator.canPop] will return false if this route's
     * [willHandlePopInternally] returns false.
     *
     * If [isFirst] and [isCurrent] are both true then this is the only route on the navigator (and
     * [isActive] will also be true).
     */
    val isFirst: Boolean
        get() {
            return navigator?.history?.first() == this
        }

    /**
     * Whether this route is on the navigator.
     *
     * If the route is not only active, but also the current route (the top-most route), then
     * [isCurrent] will also be true. If it is the first route (the bottom-most route), then
     * [isFirst] will also be true.
     *
     * If a later route is entirely opaque, then the route will be active but not rendered. It is
     * even possible for the route to be active but for the stateful widgets within the route to not
     * be instantiated. See [ModalRoute.maintainState].
     */
    val isActive: Boolean
        get() {
            return navigator?.history?.contains(this) ?: false
        }

    /**
     * Called when the route is inserted into the navigator.
     *
     * Use this to populate overlayEntries and add them to the overlay (accessible as
     * navigator.overlay). (The reason the Route is responsible for doing this, rather than the
     * Navigator, is that the Route will be responsible for _removing_ the entries and this way it's
     * symmetric.)
     *
     * The overlay argument will be null if this is the first route inserted.
     */
    open fun install(insertionPoint: Overlay.Entry?) {}

    /**
     * Called after [install] when the route is pushed onto the navigator.
     *
     * The returned value resolves when the push transition is complete.
     */
    open fun didPush(): Deferred<Unit> {
        return CompletableDeferred(Unit)
    }

    /**
     * Called after [install] when the route replaced another in the navigator.
     */
    open fun didReplace(oldRoute: Route<*>?) {}

    /**
     * Returns false if this route wants to veto a [Navigator.pop]. This method is called by
     * [Navigator.maybePop].
     *
     * By default, routes veto a pop if they're the first route in the history (i.e., if [isFirst]).
     * This behavior prevents the user from popping the first route off the history and being
     * stranded at a blank screen.
     */
    open fun willPop(): Deferred<PopDisposition> = async {
        if (isFirst) PopDisposition.BUBBLE else PopDisposition.POP
    }

    /**
     * A request was made to pop this route. If the route can handle it internally (e.g. because it
     * has its own stack of internal state) then return false, otherwise return true. Returning
     * false will prevent the default behavior of [NavigatorState.pop].
     *
     * When this function returns true, the navigator removes this route from the history but does
     * not yet call [dispose]. Instead, it is the route's responsibility to call
     * [NavigatorState.finalizeRoute], which will in turn call [dispose] on the route. This sequence
     * lets the route perform an exit animation (or some other visual effect) after being popped but
     * prior to being disposed.
     */
    open fun didPop(result: Any?): Boolean {
        didComplete(result)
        return true
    }

    /**
     * The given route, which came after this one, has been popped off the navigator.
     */
    open fun didPopNext(nextRoute: Route<*>?) {}

    /**
     * This route's next route has changed to the given new route. This is called on a route
     * whenever the next route changes for any reason, except for cases when [didPopNext] would be
     * called, so long as it is in the history. `nextRoute` will be null if there's no next route.
     */
    open fun didChangeNext(nextRoute: Route<*>?) {}

    /**
     * This route's previous route has changed to the given new route. This is called on a route
     * whenever the previous route changes for any reason, so long as it is in the history, except
     * for immediately after the route has been pushed (in which case [didPush] or [didReplace] will
     * be called instead). `previousRoute` will be null if there's no previous route.
     */
    open fun didChangePrevious(previousRoute: Route<*>?) {}

    /**
     * The route was popped or is otherwise being removed somewhat gracefully.
     *
     * This is called by [didPop] and in response to [Navigator.pushReplacement].
     */
    open fun didComplete(result: Any?) {
        popped.complete(result)
    }

    /**
     * The route should remove its overlays and free any other resources.
     *
     * This route is no longer referenced by the navigator.
     */
    open fun dispose() {
        navigator = null
    }

    /**
     * Data that might be useful in constructing a [Route].
     */
    data class Settings(
            /**
             * The name of the route (e.g., "/settings").
             *
             * If null, the route is anonymous.
             */
            val name: String?,
            /**
             * Whether this route is the very first route being pushed onto this [Navigator].
             *
             * The initial route typically skips any entrance transition to speed startup.
             */
            val isInitialRoute: Boolean
    )

    /**
     * Indicates whether the current route should be popped.
     *
     * Used as the return value for [Route.willPop].
     */
    enum class PopDisposition {
        /**
         * Pop the route.
         *
         * If [Route.willPop] returns [POP] then the back button will actually pop the current
         * route.
         */
        POP,
        /**
         * Do not pop the route.
         *
         * If [Route.willPop] returns [DO_NOT_POP] then the back button will be ignored.
         */
        DO_NOT_POP,
        /**
         * Delegate this to the next level of navigation.
         *
         * If [Route.willPop] return [] then the back button will be handled by the
         * [SystemNavigator], which will usually close the application.
         */
        BUBBLE
    }

}
