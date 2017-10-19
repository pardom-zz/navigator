package navigator.mock

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import navigator.route.AbsRoute

class MockRoute(
        override val willHandlePopInternally: Boolean = false) : AbsRoute<String>() {

    override fun didPush(): Deferred<Unit> {
        return CompletableDeferred(Unit)
    }

}
