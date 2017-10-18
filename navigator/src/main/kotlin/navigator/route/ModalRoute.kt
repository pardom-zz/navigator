package navigator.route

import navigator.Route

abstract class ModalRoute<T>(
        /**
         * The settings for this route.
         *
         * See [Route.Settings] for details.
         */
        private val settings: Route.Settings) : TransitionRoute<T>() {

    private val localHistoryRoute = LocalHistoryRouteImpl<T>()

    private class LocalHistoryRouteImpl<T> : LocalHistoryRoute<T>()

}
