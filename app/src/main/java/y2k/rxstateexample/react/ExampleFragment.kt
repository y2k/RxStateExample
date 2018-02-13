package y2k.rxstateexample.react

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_example.*
import org.koin.android.ext.android.inject
import y2k.rxstateexample.R
import y2k.rxstateexample.common.addTextChangedListener
import y2k.rxstateexample.react.ExampleComponent.Events
import y2k.rxstateexample.react.ExampleComponent.State

class ExampleFragment : Fragment() {

    private val component by inject<Component<State, Events>>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true

        // Подписка на обновления "стейта" компонента
        component.listenStateChanges { state ->
            email.error = if (state.emailValid) null else "E-mail is invalid"
            button.isEnabled = state.buttonEnabled
            button.text = state.buttonText
        }

        // Отсылка событий UI в компонент
        email.addTextChangedListener { component.acceptEvent(Events.EmailChanged(it)) }
        name.addTextChangedListener { component.acceptEvent(Events.NameChanged(it)) }
        surname.addTextChangedListener { component.acceptEvent(Events.SurnameChanged(it)) }
        button.setOnClickListener { component.acceptEvent(Events.Clicked) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        component.listenStateChanges {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_example, container, false)
}