package navigator.page

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import navigator.Overlay
import navigator.Route
import navigator.ViewBuilder
import navigator.route.AbsModalRoute
import navigator.route.TransitionRoute

class PageRoute<T>(
        private val builder: ViewBuilder,
        override val settings: Route.Settings = Route.Settings()) : AbsModalRoute<T>() {

    override val opaque: Boolean = true

    override val barrierDismissible: Boolean = false

    override val barrierDrawable: Drawable = ColorDrawable()

    override fun canTransitionTo(nextRoute: TransitionRoute<*>) = nextRoute is PageRoute

    override fun canTransitionFrom(nextRoute: TransitionRoute<*>) = nextRoute is PageRoute

    override fun createOverlayEntries(): Collection<Overlay.Entry> {
        return listOf(
                Overlay.Entry(
                        builder,
                        opaque
                )
        )
    }

}
