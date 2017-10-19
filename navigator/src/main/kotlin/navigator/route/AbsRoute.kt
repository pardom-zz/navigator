package navigator.route

import kotlinx.coroutines.experimental.CompletableDeferred
import navigator.Navigator
import navigator.Overlay
import navigator.Route

abstract class AbsRoute<T> : Route<T> {

    override var navigator: Navigator? = null

    override val overlayEntries: MutableList<Overlay.Entry> = mutableListOf()

    override val popped: CompletableDeferred<Any?> = CompletableDeferred()

    override val currentResult: T? = null

    override val willHandlePopInternally: Boolean = false

}

