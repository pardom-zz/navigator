package navigator.route

import navigator.Route

abstract class PopupRoute<T>(settings: Route.Settings) : ModalRoute<T>(settings) {

    override val opaque: Boolean = false

}
