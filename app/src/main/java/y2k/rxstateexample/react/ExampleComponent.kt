package y2k.rxstateexample.react

import y2k.rxstateexample.common.Service
import y2k.rxstateexample.common.toUser
import y2k.rxstateexample.react.ExampleComponent.Events
import y2k.rxstateexample.react.ExampleComponent.State

class ExampleComponent(private val service: Service) : AbstractComponent<State, Events>() {

    /** Обработка входящих событий. */
    override fun acceptEvent(event: Events) {
        when (event) {
            is Events.EmailChanged -> updateState(getState().copy(email = event.value))
            is Events.NameChanged -> updateState(getState().copy(name = event.value))
            is Events.SurnameChanged -> updateState(getState().copy(surname = event.value))
            Events.Clicked -> {
                service.saveUser(getState().toUser())
                updateState(getState())
            }
        }
    }

    private fun updateState(state: State) {
        val emailValid = state.email.contains("@")
        val usersEqual = service.readUser() == state.toUser()
        val newState = state.copy(
            emailValid = emailValid,
            buttonEnabled = emailValid && !usersEqual,
            buttonText = if (usersEqual) "Nothing changed" else "Save changes")
        setState(newState)
    }

    /** Создание начального стейта. */
    override fun getInitState(): State = State(buttonText = "Nothing changed")

    /** Весь "важный" для экрана UI стейт. */
    data class State(
        val email: String = "",
        val name: String = "",
        val surname: String = "",
        val buttonText: String = "",
        val buttonEnabled: Boolean = false,
        val emailValid: Boolean = true)

    /** События приходящие от UI в компонент. */
    sealed class Events {
        class EmailChanged(val value: String) : Events()
        class NameChanged(val value: String) : Events()
        class SurnameChanged(val value: String) : Events()
        object Clicked : Events()
    }
}

// *** *** *** *** *** *** *** *** *** *** *** ***
// Мини фреймворк
// *** *** *** *** *** *** *** *** *** *** *** ***

interface Component<out TState, in TEvent> {
    fun listenStateChanges(callback: (TState) -> Unit)
    fun acceptEvent(event: TEvent)
}

abstract class AbstractComponent<TState, in TEvent> : Component<TState, TEvent> {
    private var state: TState? = null
    private var callback: ((TState) -> Unit)? = null

    abstract fun getInitState(): TState

    fun setState(state: TState) {
        if (state != this.state) {
            this.state = state
            callback?.invoke(state)
        }
    }

    fun getState(): TState {
        if (state == null)
            state = getInitState()
        return state!!
    }

    override fun listenStateChanges(callback: (TState) -> Unit) {
        this.callback = callback
        callback.invoke(getState())
    }
}