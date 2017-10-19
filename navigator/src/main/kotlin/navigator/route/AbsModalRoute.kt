package navigator.route

abstract class AbsModalRoute<T> : AbsTransitionRoute<T>(), ModalRoute<T> {

    override val willHandlePopInternally: Boolean
            = super<AbsTransitionRoute>.willHandlePopInternally

}


