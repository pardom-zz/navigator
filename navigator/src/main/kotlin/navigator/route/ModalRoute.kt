package navigator.route

import android.graphics.drawable.Drawable
import navigator.Route

/**
 * A route that blocks interaction with previous routes.
 *
 * ModalRoutes cover the entire [navigator.Navigator]. They are not necessarily [opaque], however;
 * for example, a pop-up menu uses a ModalRoute but only shows the menu in a small box overlapping
 * the previous route.
 */
interface ModalRoute<T> : TransitionRoute<T>, LocalHistoryRoute<T> {

    /**
     * The settings for this route.
     *
     * See [Route.Settings] for details.
     */
    val settings: Route.Settings

    /**
     * Whether you can dismiss this route by tapping the modal barrier.
     *
     * The modal barrier is the scrim that is rendered behind each route, which generally prevents
     * the user from interacting with the route below the current route, and normally partially
     * obscures such routes.
     *
     * For example, when a dialog is on the screen, the page below the dialog is usually darkened by
     * the modal barrier.
     *
     * If [barrierDismissible] is true, then tapping this barrier will cause the current route to be
     * popped (see [navigator.Navigator.pop]) with null as the value.
     *
     * If [barrierDismissible] is false, then tapping the barrier has no effect.
     */
    val barrierDismissible: Boolean

    /**
     * The drawable to use for the modal barrier. If this is null, the barrier will be transparent.
     *
     * The modal barrier is the scrim that is rendered behind each route, which generally prevents
     * the user from interacting with the route below the current route, and normally partially
     * obscures such routes.
     *
     * For example, when a dialog is on the screen, the page below the dialog is usually darkened by
     * the modal barrier.
     *
     * While the route is animating into position, the drawable is animated from transparent to the
     * specified drawable.
     */
    val barrierDrawable: Drawable

    override fun didPop(result: Any?): Boolean {
        return super<TransitionRoute>.didPop(result)
    }

    override fun didChangePrevious(previousRoute: Route<*>?) {
        super<TransitionRoute>.didChangePrevious(previousRoute)
        navigator?.invalidate()
    }

    override fun changedInternalState() {
        super.changedInternalState()
        navigator?.invalidate()
    }

}
