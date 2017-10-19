package navigator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.experimental.Deferred

typealias ViewBuilder = (Context) -> View

typealias RoutePredicate = (Route<*>?) -> Boolean

typealias WillPopCallback = () -> Deferred<Boolean>

fun ViewGroup.resourceViewBuilder(layoutResId: Int): ViewBuilder = { context: Context ->
    LayoutInflater.from(context).inflate(layoutResId, this, false)
}
