package navigator.mock

import android.content.Context
import navigator.Navigator
import navigator.Route

class MockNavigator(
        context: Context,
        override val initialRoute: String = DEFAULT_ROUTE_NAME
) : Navigator(context) {

    val routes
        get() = getHistory()

    override fun onGenerateRoute(settings: Route.Settings): Route<*>? {
        return null
    }

    override fun onUnknownRoute(settings: Route.Settings): Route<*> {
        return MockRoute()
    }

}
