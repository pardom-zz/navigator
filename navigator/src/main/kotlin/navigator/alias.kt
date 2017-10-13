package navigator

import android.content.Context
import android.view.View

typealias ViewBuilder = (Context) -> View

typealias RoutePredicate = (Route<*>) -> Boolean
