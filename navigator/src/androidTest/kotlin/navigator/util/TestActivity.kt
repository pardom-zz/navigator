package navigator.util

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout

class TestActivity : Activity() {

    lateinit var parent: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parent = FrameLayout(this)
        setContentView(parent)
    }

}
