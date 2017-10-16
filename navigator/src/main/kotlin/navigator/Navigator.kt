package navigator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.util.concurrent.atomic.AtomicBoolean

abstract class Navigator : Overlay {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Abstract properties

    /**
     * The name of the first route to show.
     *
     * By default, this defers to [DEFAULT_ROUTE_NAME].
     *
     * If this string contains any `/` characters, then the string is split on those characters and
     * substrings from the start of the string up to each such character are, in turn, used as
     * routes to push.
     *
     * For example, if the route `/stocks/HOOLI` was used as the [initialRoute], then the
     * [Navigator] would push the following routes on startup: `/`, `/stocks`, `/stocks/HOOLI`. This
     * enables deep linking while allowing the application to maintain a predictable route history.
     */
    abstract val initialRoute: String

    /**
     * Called to generate a route for a given [Route.Settings]
     */
    abstract val onGenerateRoute: RouteFactory

    /**
     * Called when [onGenerateRoute] fails to generate a route.
     *
     * This callback is typically used for error handling. For example, this callback might always
     * generate a "not found" page that describes the route that wasn't found.
     *
     * Unknown routes can arise either from errors in the app or from external requests to push
     * routes, such as from Android intents.
     */
    abstract val onUnknownRoute: RouteFactory

    /**
     * A list of observers for this navigator.
     */
    abstract val observers: List<Observer>

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Properties

    // Overrides
    override val initialEntries = mutableListOf<Overlay.Entry>()

    // Private
    private val initialized = AtomicBoolean(false)
    internal val history = mutableListOf<Route<*>>()
    private val poppedRoutes = mutableSetOf<Route<*>>()
    private val currentOverlayEntry: Overlay.Entry?
        get() {
            history.reversed().forEach { route ->
                if (route.overlayEntries.isNotEmpty()) {
                    return route.overlayEntries.last()
                }
            }
            return null
        }

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

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public API

    /**
     * Adds the given route to the navigator's history, and transitions to it.
     *
     * The new route and the previous route (if any) are notified (see [Route.didPush] and
     * [Route.didChangeNext]). If the [Navigator] has any [Navigator.observers], they will be
     * notified as well (see [NavigatorObserver.didPush]).
     *
     * Ongoing gestures within the current route are canceled when a new route is pushed.
     *
     * Returns a [Deferred] that completes to the `result` value passed to [pop] when the pushed
     * route is popped off the navigator.
     */
    fun push(route: Route<*>): Deferred<*> {
        assert(route.navigator == null);
        val oldRoute = if (history.isNotEmpty()) history.last() else null
        route.navigator = this
        route.install(currentOverlayEntry)
        history.add(route)
        route.didPush()
        route.didChangeNext(null)
        oldRoute?.didChangeNext(route)
        observers.forEach { observer ->
            observer.didPush(route, oldRoute)
        }
        return route.popped
    }

    /**
     * Push a named route onto the navigator.
     *
     * The route name will be passed to [Navigator.onGenerateRoute]. The returned route will be
     * pushed into the navigator.
     *
     * Returns a [Deferred] that completes to the `result` value passed to [pop] when the pushed
     * route is popped off the navigator.
     *
     * Typical usage is as follows:
     *
     * ```
     * Navigator.of(view).pushNamed('/nyc/1776')
     * ```
     */
    fun pushNamed(name: String): Deferred<*> {
        return push(routeNamed(name))
    }

    /**
     * Replaces a route that is not currently visible with a new route.
     *
     * The new route and the route below the new route (if any) are notified (see [Route.didReplace]
     * and [Route.didChangeNext]). The navigator observer is not notified. The old route is disposed
     * (see [Route.dispose]).
     *
     * This can be useful in combination with [removeRouteBelow] when building a non-linear user
     * experience.
     */
    fun replace(oldRoute: Route<*>, newRoute: Route<*>) {
        if (oldRoute == newRoute) return
        assert(oldRoute.navigator == this)
        assert(newRoute.navigator == null)
        assert(oldRoute.overlayEntries.isNotEmpty())
        assert(newRoute.overlayEntries.isEmpty())
        val index = history.indexOf(oldRoute)
        assert(index >= 0)
        newRoute.navigator = this
        newRoute.install(oldRoute.overlayEntries.lastOrNull())
        history[index] = newRoute
        newRoute.didReplace(oldRoute)
        if (index + 1 < history.size) {
            newRoute.didChangeNext(history[index + 1])
            history[index + 1].didChangePrevious(newRoute)
        }
        else {
            newRoute.didChangeNext(null)
        }
        if (index > 0) {
            history[index - 1].didChangeNext(newRoute)
        }
        oldRoute.dispose()
    }

    fun pushReplacement(newRoute: Route<*>, result: Any? = null): Deferred<*> {
        val oldRoute = history.last()
        assert(oldRoute.navigator == this)
        assert(oldRoute.overlayEntries.isNotEmpty())
        assert(newRoute.navigator == null)
        assert(newRoute.overlayEntries.isEmpty())
        val index = history.size - 1
        assert(index >= 0)
        assert(history.indexOf(oldRoute) == index)
        newRoute.navigator = this
        newRoute.install(currentOverlayEntry)
        history[index] = newRoute
        newRoute.didPush().invokeOnCompletion {
            oldRoute.didComplete(null)
            oldRoute.dispose()
        }
        newRoute.didChangeNext(null)
        if (index > 0) {
            history[index - 1].didChangeNext(newRoute)
        }
        observers.forEach { observer ->
            observer.didPush(newRoute, oldRoute)
        }
        return newRoute.popped
    }

    fun pushReplacementNamed(name: String, result: Any? = null): Deferred<*> {
        return pushReplacement(routeNamed(name), result)
    }

    fun replaceRouteBelow(anchorRoute: Route<*>, newRoute: Route<*>) {

    }

    fun removeRouteBelow(anchorRoute: Route<*>) {

    }

    fun pushAndRemoveUntil(newRoute: Route<*>, predicate: RoutePredicate): Deferred<*> {
        TODO()
    }

    fun pushNamedAndRemoveUntil(name: String, predicate: RoutePredicate): Deferred<*> {
        return pushAndRemoveUntil(routeNamed(name), predicate)
    }

    fun maybePop(result: Any? = null): Deferred<*> = async {
        TODO()
    }

    fun pop(result: Any? = null): Boolean {
        TODO()
    }

    fun removeRoute(route: Route<*>) {
    }

    fun finalizeRoute(route: Route<*>) {

    }

    fun popUntil(predicate: RoutePredicate) {

    }

    fun canPop(): Boolean {
        TODO()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected functions

    protected fun getHistory(): List<Route<*>> {
        return history
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Private functions

    private fun routeNamed(name: String): Route<*> {
        return nullableRouteNamed(name) ?:
                onUnknownRoute(Route.Settings(name, history.isEmpty())) ?:
                error("""A Navigator's onUnknownRoute returned null.
                    When trying to build the route "$name", both onGenerateRoute and onUnknownRoute
                    returned null. The onUnknownRoute callback should never return null.
                    The Navigator was:
                      $this""".trimIndent())
    }

    private fun nullableRouteNamed(name: String): Route<*>? {
        return onGenerateRoute(Route.Settings(name, history.isEmpty()))
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Classes

    /**
     * An interface for observing the behavior of a [Navigator].
     */
    interface Observer {

        /**
         * The navigator that the observer is observing, if any.
         */
        var navigator: Navigator?

        /**
         * The [Navigator] pushed `route`.
         */
        fun didPush(route: Route<*>, previousRoute: Route<*>?)

        /**
         * The [Navigator] popped `route`.
         */
        fun didPop(route: Route<*>, previousRoute: Route<*>?)

        /**
         * The [Navigator] removed `route`.
         */
        fun didRemove(route: Route<*>, previousRoute: Route<*>?)

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Companion

    companion object {

        /**
         * The default name for the [initialRoute].
         */
        const val DEFAULT_ROUTE_NAME = "/"

        /**
         *
         */
        fun push(view: View, route: Route<*>)
                = of(view).push(route)

        /**
         *
         */
        fun pushNamed(view: View, name: String)
                = of(view).pushNamed(name)

        /**
         *
         */
        fun pushReplacement(view: View, newRoute: Route<*>, result: Any? = null): Deferred<*>
                = of(view).pushReplacement(newRoute, result)

        /**
         *
         */
        fun pushReplacementNamed(view: View, name: String, result: Any? = null): Deferred<*>
                = of(view).pushReplacementNamed(name, result)

        /**
         *
         */
        fun maybePop(view: View, result: Any? = null): Deferred<*>
                = of(view).maybePop(result)

        /**
         *
         */
        fun pop(view: View, result: Any? = null): Boolean
                = of(view).pop(result)

        /**
         *
         */
        fun removeRoute(view: View, route: Route<*>)
                = of(view).removeRoute(route)

        /**
         *
         */
        fun popUntil(view: View, predicate: RoutePredicate)
                = of(view).popUntil(predicate)

        /**
         *
         */
        fun canPop(view: View): Boolean
                = of(view).canPop()

        /**
         *
         */
        fun popAndPushNamed(view: View, name: String, result: Any? = null): Deferred<*> {
            val navigator = of(view)
            navigator.pop(result)
            return navigator.pushNamed(name)
        }

        /**
         * The state from the closest instance of this class that encloses the given view.
         *
         * Typical usage is as follows:
         *
         * ```
         * Navigator.of(view)
         *     .apply { pop() }
         *     .apply { pop() }
         *     .pushNamed('/settings')
         * ```
         */
        tailrec fun of(view: View): Navigator {
            tailrec fun of(viewParent: ViewParent?): Navigator {
                if (viewParent is Navigator) return viewParent
                if (viewParent == null) error("Overlay not an ancestor of $view.")
                return of(viewParent.parent)
            }
            if (view is Navigator) return view
            return of(view.parent)
        }

    }

}
