package navigator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A widget that manages a set of child widgets with a stack discipline.
 *
 * Many apps have a navigator near the top of their widget hierarchy in order to display their
 * logical history using an [Overlay] with the most recently visited pages visually on top of the
 * older pages. Using this pattern lets the navigator visually transition from one page to another
 * by moving the widgets around in the overlay. Similarly, the navigator can be used to show a
 * dialog by positioning the dialog widget above the current page.
 *
 * ## Using the Navigator
 *
 * Mobile apps typically reveal their contents via full-screen elements called "screens" or "pages".
 * In Navigator these elements are called routes and they're managed by a [Navigator] widget. The
 * navigator manages a stack of [Route] objects and provides methods for managing the stack, like
 * [Navigator.push] and [Navigator.pop].
 *
 * ### Displaying a full-screen route
 *
 * To push a new route on the stack you can create an instance of [PageRoute] with a builder
 * function that creates whatever you want to appear on the screen. For example:
 *
 * ```
 * Navigator.of(context).push(new PageRoute<Unit>(
 *     builder: (BuildContext context) {
 *          return new Scaffold(
 *              appBar: new AppBar(title: new Text('My Page')),
 *              body: new Center(
 *                  child: new FlatButton(
 *                      child: new Text('POP'),
 *                      onPressed: () {
 *                          Navigator.of(context).pop();
 *                      },
 *                  ),
 *              ),
 *          );
 *      },
 * ));
 * ```
 *
 * The route defines its widget with a builder function instead of a child widget because it will be
 * built and rebuilt in different contexts depending on when it's pushed and popped.
 *
 * As you can see, the new route can be popped, revealing the app's home page, with the Navigator's
 * pop method:
 *
 * ```
 * Navigator.of(view).pop()
 * ```
 *
 * It usually isn't necessary to provide a widget that pops the Navigator in a route with a
 * [Scaffold] because the Scaffold automatically adds a 'back' button to its AppBar. Pressing the
 * back button causes [Navigator.pop] to be called. On Android, pressing the system back button does
 * the same thing.
 *
 * ### Using named navigator routes
 *
 * Mobile apps often manage a large number of routes and it's often easiest to refer to them by
 * name. Route names, by convention, use a path-like structure (for example, '/a/b/c'). The app's
 * home page route is named '/' by default.
 *
 * The [MaterialApp] can be created with a [Map<String, WidgetBuilder>] which maps from a route's
 * name to a builder function that will create it. The [MaterialApp] uses this map to create a value
 * for its navigator's [onGenerateRoute] callback.
 *
 * ```dart
 * void main() {
 * runApp(new MaterialApp(
 * home: new MyAppHome(), // becomes the route named '/'
 * routes: <String, WidgetBuilder> {
 * '/a': (BuildContext context) => new MyPage(title: 'page A'),
 * '/b': (BuildContext context) => new MyPage(title: 'page B'),
 * '/c': (BuildContext context) => new MyPage(title: 'page C'),
 * *
 * ));
 * }
 * ```
 *
 * To show a route by name:
 *
 * ```dart
 * Navigator.of(context).pushNamed('/b');
 * ```
 *
 * ### Routes can return a value
 *
 * When a route is pushed to ask the user for a value, the value can be returned via the [pop]
 * method's result parameter.
 *
 * Methods that push a route return a Future. The Future resolves when the route is popped and the
 * Future's value is the [pop] method's result parameter.
 *
 * For example if we wanted to ask the user to press 'OK' to confirm an operation we could `await`
 * the result of [Navigator.push]:
 *
 * ```
 * bool value = await Navigator.of(context).push(new MaterialPageRoute<bool>(
 * builder: (BuildContext context) {
 * return new Center(
 * child: new GestureDetector(
 * child: new Text('OK'),
 * onTap: () { Navigator.of(context).pop(true); }
 * ),
 * );
 * }
 * ));
 * ```
 * If the user presses 'OK' then value will be true. If the user backs out of the route, for example
 * by pressing the Scaffold's back button, the value will be null.
 *
 * When a route is used to return a value, the route's type parameter must match the type of [pop]'s
 * result. That's why we've used `MaterialPageRoute<bool>` instead of `MaterialPageRoute<Null>`.
 *
 * ### Popup routes
 *
 * Routes don't have to obscure the entire screen. [PopupRoute]s cover the screen with a
 * [ModalRoute.barrierColor] that can be only partially opaque to allow the current screen to show
 * through. Popup routes are "modal" because they block input to the widgets below.
 *
 * There are functions which create and show popup routes. For example: [showDialog], [showMenu],
 * and [showModalBottomSheet]. These functions return their pushed route's Future as described
 * above. Callers can await the returned value to take an action when the route is popped, or to
 * discover the route's value.
 *
 * There are also widgets which create popup routes, like [PopupMenuButton] and [DropdownButton].
 * These widgets create internal subclasses of PopupRoute and use the Naviagator's push and pop
 * methods to show and dismiss them.
 *
 * ### Custom routes
 *
 * You can create your own subclass of one the widget library route classes like [PopupRoute],
 * [ModalRoute], or [PageRoute], to control the animated transition employed to show the route, the
 * color and behavior of the route's modal barrier, and other aspects of the route.
 *
 * The PageRouteBuilder class makes it possible to define a custom route in terms of callbacks.
 * Here's an example that rotates and fades its child when the route appears or disappears. This
 * route does not obscure the entire screen because it specifies `opaque: false`, just as a popup
 * route does.
 *
 * ```dart
 * Navigator.of(context).push(new PageRouteBuilder(
 * opaque: false,
 * pageBuilder: (BuildContext context, _, __) {
 * return new Center(child: new Text('My PageRoute'));
 * },
 * transitionsBuilder: (_, Animation<double> animation, __, Widget child) {
 * return new FadeTransition(
 * opacity: animation,
 * child: new RotationTransition(
 * turns: new Tween<double>(begin: 0.5, end: 1.0).animate(animation),
 * child: child,
 * ),
 * );
 * }
 * ));
 * ```
 *
 * The page route is built in two parts, the "page" and the "transitions". The page becomes a
 * descendant of the child passed to the `buildTransitions` method. Typically the page is only built
 * once, because it doesn't depend on its animation parameters (elided with `_` and `__` in this
 * example). The transition is built on every frame for its duration.
 */
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
    open val initialRoute: String? = null

    /**
     * Called to generate a route for a given [Route.Settings]
     */
    abstract fun onGenerateRoute(settings: Route.Settings): Route<*>?

    /**
     * Called when [onGenerateRoute] fails to generate a route.
     *
     * This callback is typically used for error handling. For example, this callback might always
     * generate a "not found" page that describes the route that wasn't found.
     *
     * Unknown routes can arise either from errors in the app or from external requests to push
     * routes, such as from Android intents.
     */
    abstract fun onUnknownRoute(settings: Route.Settings): Route<*>

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Properties

    // Overrides
    override val initialEntries = mutableListOf<Overlay.Entry>()

    // Internal
    internal val history = mutableListOf<Route<*>>()

    // Private
    private val observers = mutableListOf<Observer>()
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
            observers.forEach { observer ->
                assert(observer.navigator == null)
                observer.navigator = this
            }
            val initialRouteName = initialRoute ?: DEFAULT_ROUTE_NAME
            if (initialRouteName.startsWith("/") && initialRouteName.length > 1) {

            }
            else {
                var route: Route<*>? = null
                if (initialRouteName != DEFAULT_ROUTE_NAME) {
                    route = nullableRouteNamed(initialRouteName)
                }
                if (route == null) {
                    route = routeNamed(DEFAULT_ROUTE_NAME)
                }
                push(route)
            }
            history.forEach { route ->
                initialEntries.addAll(route.overlayEntries)
            }
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

    /**
     * Push the [newRoute] and dispose the old current Route.
     *
     * The new route and the route below the new route (if any) are notified (see [Route.didPush]
     * and [Route.didChangeNext]). The navigator observer is not notified about the old route. The
     * old route is disposed (see [Route.dispose]). The new route is not notified when the old route
     * is removed (which happens when the new route's animation completes).
     *
     * If a [result] is provided, it will be the return value of the old route, as if the old route
     * had been popped.
     */
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
            oldRoute.didComplete(result ?: oldRoute.currentResult)
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

    /**
     * Push the route named [name] and dispose the old current route.
     *
     * The route name will be passed to [Navigator.onGenerateRoute]. The returned route will be
     * pushed into the navigator.
     *
     * Returns a [Future] that completes to the `result` value passed to [pop] when the pushed route
     * is popped off the navigator.
     */
    fun pushReplacementNamed(name: String, result: Any? = null): Deferred<*> {
        return pushReplacement(routeNamed(name), result)
    }

    /**
     * Replaces a route that is not currently visible with a new route.
     *
     * The route to be removed is the one below the given `anchorRoute`. That route must not be the
     * first route in the history.
     *
     * In every other way, this acts the same as [replace].
     */
    fun replaceRouteBelow(anchorRoute: Route<*>, newRoute: Route<*>) {
        assert(anchorRoute.navigator == this)
        assert(history.indexOf(anchorRoute) > 0)
        replace(history[history.indexOf(anchorRoute) - 1], newRoute)
    }

    /**
     * Removes the route below the given `anchorRoute`. The route to be removed must not currently
     * be visible. The `anchorRoute` must not be the first route in the history.
     *
     * The removed route is disposed (see [Route.dispose]). The route prior to the removed route, if
     * any, is notified (see [Route.didChangeNext]). The route above the removed route, if any, is
     * also notified (see [Route.didChangePrevious]). The navigator observer is not notified.
     */
    fun removeRouteBelow(anchorRoute: Route<*>) {
        assert(anchorRoute.navigator == this)
        val index = history.indexOf(anchorRoute) - 1
        assert(index >= 0)
        val targetRoute = history[index]
        assert(targetRoute.navigator == this)
        assert(targetRoute.overlayEntries.isEmpty())
        history.removeAt(index)
        val nextRoute = history.getOrNull(index)
        val previousRoute = history.getOrNull(index - 1)
        previousRoute?.didChangeNext(nextRoute)
        nextRoute?.didChangePrevious(previousRoute)
        targetRoute.dispose()
    }

    /**
     * Push the given route and then remove all the previous routes until the `predicate` returns
     * true.
     *
     * The predicate may be applied to the same route more than once if
     * [Route.willHandlePopInternally] is true.
     *
     * To remove routes until a route with a certain name, use the [RoutePredicate] returned from
     * [ModalRoute.withName].
     *
     * To remove all the routes before the pushed route, use a [RoutePredicate] that always returns
     * false.
     */
    fun pushAndRemoveUntil(newRoute: Route<*>, predicate: RoutePredicate): Deferred<*> {
        val removedRoutes = mutableListOf<Route<*>>()
        while (history.isNotEmpty() && !predicate(history.last())) {
            val removedRoute = history.removeAt(history.lastIndex)
            assert(removedRoute.navigator == this)
            assert(removedRoute.overlayEntries.isNotEmpty())
            removedRoutes.add(removedRoute)
        }
        assert(newRoute.navigator == null)
        assert(newRoute.overlayEntries.isEmpty())
        val oldRoute = history.lastOrNull()
        newRoute.navigator = this
        newRoute.install(currentOverlayEntry)
        history.add(newRoute)
        newRoute.didPush().invokeOnCompletion {
            removedRoutes.forEach { route ->
                route.dispose()
            }
        }
        newRoute.didChangeNext(null)
        oldRoute?.didChangeNext(newRoute)
        observers.forEach { observer ->
            observer.didPush(newRoute, oldRoute)
        }
        return newRoute.popped
    }

    /**
     * Push the route with the given name and then remove all the previous routes until the
     * `predicate` returns true.
     *
     * The predicate may be applied to the same route more than once if
     * [Route.willHandlePopInternally] is true.
     *
     * To remove routes until a route with a certain name, use the [RoutePredicate] returned from
     * [ModalRoute.withName].
     *
     * To remove all the routes before the pushed route, use a [RoutePredicate] that always returns
     * false.
     */
    fun pushNamedAndRemoveUntil(name: String, predicate: RoutePredicate): Deferred<*> {
        return pushAndRemoveUntil(routeNamed(name), predicate)
    }

    /**
     * Tries to pop the current route, first giving the active route the chance to veto the
     * operation using [Route.willPop]. This method is typically called instead of [pop] when the
     * user uses a back button.
     *
     * @see [ModalRoute], which has as a [ModalRoute.willPop] method that can be defined by a list
     * of [WillPopCallback]s.
     */
    fun maybePop(result: Any? = null): Deferred<Boolean> = async {
        val route = history.last()
        assert(route.navigator == this)
        val disposition = route.willPop().await()
        if (disposition != Route.PopDisposition.BUBBLE) {
            if (disposition == Route.PopDisposition.POP) {
                pop(result)
            }
            true
        }
        else false
    }

    /**
     * Removes the top route in the [Navigator]'s history.
     *
     * If an argument is provided, that argument will be the return value of the route (see
     * [Route.didPop]).
     *
     * If there are any routes left on the history, the top remaining route is notified (see
     * [Route.didPopNext]), and the method returns true. In that case, if the [Navigator] has any
     * [Navigator.observers], they will be notified as well (see [NavigatorObserver.didPop]).
     * Otherwise, if the popped route was the last route, the method returns false.
     *
     * Ongoing gestures within the current route are canceled when a route is popped.
     */
    fun pop(result: Any? = null): Boolean {
        val route = history.last()
        assert(route.navigator == this)
        if (route.didPop(result ?: route.currentResult)) {
            if (history.size > 1) {
                history.removeAt(history.lastIndex)
                if (route.navigator != null) {
                    poppedRoutes.add(route)
                }
                history.last().didPopNext(route)
                observers.forEach { observer ->
                    observer.didPop(route, history.last())
                }
            }
            else {
                return false
            }
        }
        return true
    }

    /**
     * Immediately remove `route` and [Route.dispose] it.
     *
     * The route's animation does not run and the future returned from pushing the route will not
     * complete. Ongoing input gestures are cancelled. If the [Navigator] has any
     * [Navigator.observers], they will be notified with [NavigatorObserver.didRemove].
     *
     * This method is used to dismiss dropdown menus that are up when the screen's orientation
     * changes.
     */
    fun removeRoute(route: Route<*>) {
        assert(route.navigator == this)
        val index = history.indexOf(route)
        assert(index != -1)
        val previousRoute = history.getOrNull(index - 1)
        val nextRoute = history.getOrNull(index + 1)
        history.removeAt(index)
        previousRoute?.didChangeNext(nextRoute)
        nextRoute?.didChangePrevious(previousRoute)
        observers.forEach { observer ->
            observer.didRemove(route, previousRoute)
        }
        route.dispose()
    }

    /**
     * Complete the lifecycle for a route that has been popped off the navigator.
     *
     * When the navigator pops a route, the navigator retains a reference to the route in order to
     * call [Route.dispose] if the navigator itself is removed from the tree. When the route is
     * finished with any exit animation, the route should call this function to complete its
     * lifecycle (e.g., to receive a call to [Route.dispose]).
     *
     * The given `route` must have already received a call to [Route.didPop]. This function may be
     * called directly from [Route.didPop] if [Route.didPop] will return true.
     */
    fun finalizeRoute(route: Route<*>) {
        poppedRoutes.remove(route)
        route.dispose()
    }

    /**
     * Repeatedly calls [pop] until the given `predicate` returns true.
     *
     * The predicate may be applied to the same route more than once if
     * [Route.willHandlePopInternally] is true.
     *
     * To pop until a route with a certain name, use the [RoutePredicate] returned from
     * [ModalRoute.withName].
     */
    fun popUntil(predicate: RoutePredicate) {
        while (!predicate(history.lastOrNull())) {
            pop()
        }
    }

    /**
     * Whether this navigator can be popped.
     *
     * The only route that cannot be popped off the navigator is the initial route.
     */
    fun canPop(): Boolean {
        assert(history.isNotEmpty())
        return history.size > 1 || history.firstOrNull()?.willHandlePopInternally ?: false
    }

    /**
     * Adds a [Navigator.Observer].
     */
    fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    /**
     * Removes the specified [Navigator.Observer].
     */
    fun removeObserver(observer: Observer): Boolean {
        return observers.remove(observer)
    }

    /**
     * Removes all [Navigator.Observer]s.
     */
    fun clearObservers() {
        observers.clear()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected functions

    protected fun getHistory(): List<Route<*>> {
        return history
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Private functions

    private fun routeNamed(name: String): Route<*> {
        return nullableRouteNamed(name) ?: onUnknownRoute(Route.Settings(name, history.isEmpty()))
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
