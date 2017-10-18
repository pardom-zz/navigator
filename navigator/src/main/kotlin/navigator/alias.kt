package navigator

import android.content.Context
import android.view.View
import kotlinx.coroutines.experimental.Deferred

typealias ViewBuilder = (Context) -> View

typealias RoutePredicate = (Route<*>?) -> Boolean

typealias WillPopCallback = () -> Deferred<Boolean>
