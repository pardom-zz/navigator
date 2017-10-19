package navigator.route

abstract class AbsModalRoute<T> : AbsTransitionRoute<T>(), ModalRoute<T> {

    override val willHandlePopInternally: Boolean
            = super<AbsTransitionRoute>.willHandlePopInternally

    override val localHistory: MutableList<LocalHistoryRoute.Entry> = mutableListOf()

}


