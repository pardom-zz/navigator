package navigator.route

interface PopupRoute<T> : ModalRoute<T> {

    override val opaque: Boolean
        get() {
            return false
        }


}
