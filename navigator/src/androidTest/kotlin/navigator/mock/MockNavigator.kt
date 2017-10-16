package navigator.mock

import android.content.Context
import navigator.Navigator
import navigator.Route
import navigator.RouteFactory

class MockNavigator(
        context: Context,
        override val initialRoute: String = DEFAULT_ROUTE_NAME,
        override val onGenerateRoute: RouteFactory = MockNavigator.onGenerateRoute,
        override val onUnknownRoute: RouteFactory = MockNavigator.onUnknownRoute,
        override val observers: List<Observer> = emptyList()
) : Navigator(context) {

    val routes
        get() = getHistory()

    companion object {

        private val onGenerateRoute = { settings: Route.Settings ->
            null
        }

        private val onUnknownRoute = { settings: Route.Settings ->
            MockRoute()
        }

    }

}
