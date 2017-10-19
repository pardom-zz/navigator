package navigator.page

import navigator.route.AbsModalRoute
import navigator.route.TransitionRoute

abstract class PageRoute<T> : AbsModalRoute<T>() {

    override val opaque: Boolean = true

    override val barrierDismissible: Boolean = false

    override fun canTransitionTo(nextRoute: TransitionRoute<*>) = nextRoute is PageRoute

    override fun canTransitionFrom(nextRoute: TransitionRoute<*>) = nextRoute is PageRoute

}
