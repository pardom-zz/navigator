package navigator.route

import android.animation.Animator
import kotlinx.coroutines.experimental.CompletableDeferred

abstract class AbsTransitionRoute<T> : AbsOverlayRoute<T>(), TransitionRoute<T> {

    override val completed: CompletableDeferred<T> = CompletableDeferred()

    override var result: T? = null

    override val animatorIn: Animator by lazy { createAnimatorIn() }

    override val animatorOut: Animator by lazy { createAnimatorOut() }

}
