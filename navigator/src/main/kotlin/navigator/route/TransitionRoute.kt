package navigator.route

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import navigator.Route

/**
 * A route with entrance and exit transitions.
 */
interface TransitionRoute<T> : OverlayRoute<T> {

    /**
     * Whether the route obscures previous routes when the transition is complete.
     *
     * When an opaque route's entrance transition is complete, the routes behind the opaque route
     * will not be built to save resources.
     */
    val opaque: Boolean

    /**
     * This future completes only once the transition itself has finished, after the overlay entries
     * have been removed from the navigator's overlay.
     *
     * This future completes once the animation has been dismissed. That will be after [popped],
     * because [popped] completes before the animation even starts, as soon as the route is popped.
     */
    val completed: CompletableDeferred<T>

    var result: T?
    var animatorIn: Animator
    var animatorOut: Animator

    /**
     * Called to create the animator that will drive the transitions to this route from the previous
     * one.
     */
    fun createAnimatorIn(): Animator {
        return AnimatorSet()
    }

    /**
     * Called to create the animator that will drive the transitions from this route to the previous
     * one.
     */
    fun createAnimatorOut(): Animator {
        return AnimatorSet()
    }

    /**
     * Whether this route can perform a transition to the given route.
     *
     * Subclasses can override this method to restrict the set of routes they need to coordinate
     * transitions with.
     */
    fun canTransitionTo(nextRoute: TransitionRoute<*>) = true

    /**
     * Whether this route can perform a transition from the given route.
     *
     * Subclasses can override this method to restrict the set of routes they need to coordinate
     * transitions with.
     */
    fun canTransitionFrom(previousRoute: TransitionRoute<*>) = true

    override fun didPush(): Deferred<Unit> {
        animatorIn.addListener(createAnimatorListener())
        animatorIn.start()
        return CompletableDeferred()
    }

    override fun didReplace(oldRoute: Route<*>?) {
        if (oldRoute is TransitionRoute<*>) {
        }
        animatorIn.addListener(createAnimatorListener())
        super.didReplace(oldRoute)
    }

    override fun didPop(result: Any?): Boolean {
        this.result = result as T
        animatorIn.cancel()
        animatorOut.start()
        return super.didPop(result)
    }

    override fun dispose() {
        animatorIn.end()
        animatorOut.end()
        completed.complete(result!!)
    }

    private fun createAnimatorListener() = object : AnimatorListener {

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationEnd(animator: Animator) {
            if (overlayEntries.isNotEmpty()) {
                overlayEntries.first().opaque = opaque
            }
        }

        override fun onAnimationCancel(animator: Animator) {
            assert(!overlayEntries.first().opaque)
            if (!isCurrent) {
                navigator?.finalizeRoute(this@TransitionRoute)
                assert(overlayEntries.isEmpty())
            }
        }

        override fun onAnimationStart(animator: Animator) {
            if (overlayEntries.isNotEmpty()) {
                overlayEntries.first().opaque = false
            }
        }

    }

}
