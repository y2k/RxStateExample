package y2k.rxstateexample.react

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_example.*
import org.koin.android.ext.android.inject
import y2k.rxstateexample.R
import y2k.rxstateexample.common.onTextChanged

class ExampleFragment : Fragment() {

    private val component: ExampleComponent by inject()

    override fun onResume() {
        super.onResume()
        retainInstance = true

        // Подписка на обновления "стейта" компонента
        component.onStateChanged { state ->
            email.error = if (state.emailValid) null else "E-mail is invalid"
            button.isEnabled = state.buttonEnabled
            button.text = state.buttonText
        }

        // Отсылка событий UI в компонент
        email.onTextChanged { component.update(ExampleComponent.Events.EmailChanged(it)) }
        name.onTextChanged { component.update(ExampleComponent.Events.NameChanged(it)) }
        surname.onTextChanged { component.update(ExampleComponent.Events.SurnameChanged(it)) }
        button.setOnClickListener { component.update(ExampleComponent.Events.Clicked) }
    }

    override fun onPause() {
        super.onPause()
        component.onStateChanged(null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_example, container, false)
}