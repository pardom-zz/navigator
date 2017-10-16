package navigator.mock

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import navigator.Navigator
import navigator.Overlay
import navigator.Route

class MockRoute : Route<String> {

    override var navigator: Navigator? = null
    override val overlayEntries: MutableList<Overlay.Entry> = mutableListOf()
    override val popped: CompletableDeferred<String?> = CompletableDeferred()
    override val currentResult: String? = null

    override fun install(insertionPoint: Overlay.Entry?) {
    }

    override fun didPush(): Deferred<Unit> {
        return CompletableDeferred()
    }

    override fun didReplace(oldRoute: Route<*>?) {
    }

    override fun didPopNext(nextRoute: Route<*>?) {
    }

    override fun didChangeNext(nextRoute: Route<*>?) {
    }

    override fun didChangePrevious(previousRoute: Route<*>?) {
    }

    override fun didComplete(result: String?) {
    }

}
