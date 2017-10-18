package navigator.mock

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import navigator.Route

class MockRoute(
        override val willHandlePopInternally: Boolean = false) : Route<String>() {

    override fun didPush(): Deferred<Unit> {
        return CompletableDeferred(Unit)
    }

}
