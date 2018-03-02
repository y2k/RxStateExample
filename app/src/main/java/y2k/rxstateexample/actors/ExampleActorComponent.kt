package y2k.rxstateexample.actors

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import y2k.rxstateexample.actors.ExampleActorComponent.Events
import y2k.rxstateexample.actors.ExampleActorComponent.State
import y2k.rxstateexample.common.Service
import y2k.rxstateexample.common.User

class ExampleActorComponent(private val service: Service) : ActorComponent<State, Events>() {

    override fun getInitState() = State(buttonText = "Nothing changed")

    override suspend fun update(event: Events, state: State) {
        val newState = when (event) {
            is Events.EmailChanged ->
                updateState(state.copy(email = event.value))
            is Events.NameChanged ->
                updateState(state.copy(name = event.value))
            is Events.SurnameChanged ->
                updateState(state.copy(surname = event.value))
            Events.Clicked -> {
                service.saveUser(state.toUser())
                updateState(state)
            }
        }
        setState(newState)
    }

    private fun updateState(s: State): State {
        val emailValid = s.email.contains("@")
        val usersEqual = service.readUser() == s.toUser()
        return s.copy(
            emailValid = emailValid,
            buttonEnabled = emailValid && !usersEqual,
            buttonText = if (usersEqual) "Nothing changed" else "Save changes")
    }

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

    private fun State.toUser(): User = User(email, name, surname)
}

abstract class ActorComponent<TState : Any, in TEvent> {

    private val outChannel = Channel<TState>()
    private val inChannel = Channel<TEvent>(32)

    abstract fun getInitState(): TState
    abstract suspend fun update(event: TEvent, state: TState)

    @Volatile
    private lateinit var state: TState
    private var started: Boolean = false

    protected suspend fun setState(state: TState) {
        this.state = state
        outChannel.send(state)
    }

    fun listenUpdates(f: (TState) -> Unit): Job {
        if (!started) {
            started = true
            start()
        }
        return launch(UI) {
            while (true)
                f(outChannel.receive())
        }
    }

    private fun start() {
        launch {
            setState(getInitState())
            while (true) {
                update(inChannel.receive(), state)
            }
        }
    }

    fun sendEvent(event: TEvent) {
        inChannel.offer(event)
    }
}