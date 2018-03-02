package y2k.rxstateexample.common

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import y2k.rxstateexample.actors.ExampleActorFragment
import y2k.rxstateexample.react.ExampleFragment

class ExampleActorActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, ExampleActorFragment())
                .commit()
    }
}

class ExampleActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, ExampleFragment())
                .commit()
    }
}