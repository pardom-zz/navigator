package navigator.example.simple

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import navigator.Navigator
import navigator.Route
import navigator.page.PageRoute
import navigator.resourceViewBuilder

class SimpleNavigator : Navigator {

    private val inflater by lazy { LayoutInflater.from(context) }

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
    // Navigator implementation

    override fun onGenerateRoute(settings: Route.Settings): Route<*>? {
        return when (settings.name) {
            else -> null
        }
    }

    override fun onUnknownRoute(settings: Route.Settings): Route<*> {
        return PageRoute<Unit>(resourceViewBuilder(R.layout.unknown_route))
    }

}
