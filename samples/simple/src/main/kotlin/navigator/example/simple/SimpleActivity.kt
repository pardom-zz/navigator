package navigator.example.simple

import android.app.Activity
import android.os.Bundle
import navigator.Navigator
import navigator.example.simple.R

class SimpleActivity : Activity() {

    private val navigator: Navigator by lazy { findViewById<Navigator>(R.id.navigator) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
    }

    override fun onBackPressed() {
        if (navigator.canPop()) {
            navigator.pop()
            return
        }
        super.onBackPressed()
    }

}
