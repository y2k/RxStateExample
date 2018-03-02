package y2k.rxstateexample.actors

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_example.*
import kotlinx.coroutines.experimental.Job
import org.koin.android.ext.android.inject
import y2k.rxstateexample.R
import y2k.rxstateexample.actors.ExampleActorComponent.Events
import y2k.rxstateexample.common.onTextChanged

class ExampleActorFragment : Fragment() {

    private val component: ExampleActorComponent by inject()
    private lateinit var job: Job

    override fun onResume() {
        super.onResume()

        job = component.listenUpdates { state ->
            email.error = if (state.emailValid) null else "E-mail is invalid"
            button.isEnabled = state.buttonEnabled
            button.text = state.buttonText
        }

        // Отсылка событий UI в компонент
        email.onTextChanged { component.sendEvent(Events.EmailChanged(it)) }
        name.onTextChanged { component.sendEvent(Events.NameChanged(it)) }
        surname.onTextChanged { component.sendEvent(Events.SurnameChanged(it)) }
        button.setOnClickListener { component.sendEvent(Events.Clicked) }
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_example, container, false)
}