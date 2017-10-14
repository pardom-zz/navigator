package navigator

import android.content.Context
import android.view.View

typealias ViewBuilder = (Context) -> View

typealias RouteFactory = (Route.Settings) -> Route<*>?

typealias RoutePredicate = (Route<*>) -> Boolean
