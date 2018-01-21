package y2k.rxstateexample

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import y2k.rxstateexample.Result.*
import y2k.rxstateexample.Services.SearchEngine.Google
import y2k.rxstateexample.Services.SearchEngine.Yandex
import y2k.rxstateexample.StateManager.Event.SearchGoogle
import y2k.rxstateexample.StateManager.Event.SearchYandex
import y2k.rxstateexample.StateManager.Model

object StateManager {

    /** Состояние всей программы */
    data class Model(
        /** Поиск в процессе */
        val isFlight: Boolean,
        /** Результаты поиска */
        val searchResults: List<String>,
        /** Ошибка последнего запроса */
        val error: String?)

    /** События от UI */
    sealed class Event {
        /** Искать в Google */
        class SearchGoogle(val query: String) : Event()

        /** Искать в Yandex */
        class SearchYandex(val query: String) : Event()
    }

    /** Начальное состояние программы */
    val init = Model(false, emptyList(), null)

    /** Создать асинхронный запрос по заданному эвенте */
    fun createObservable(event: Event): Observable<List<String>> =
        when (event) {
            is SearchGoogle -> Services.search(event.query, Google)
            is SearchYandex -> Services.search(event.query, Yandex)
        }

    /** Обновляет состояние программы в зависимости от евентов */
    fun update(model: Model, result: Result<List<String>, Throwable>): Model =
        when (result) {
            InFlight -> model.copy(isFlight = true, error = null)
            is Success -> model.copy(isFlight = false, searchResults = result.value)
            is Failure -> {
                result.error.printStackTrace()
                model.copy(isFlight = false,
                    searchResults = emptyList(),
                    error = result.error.message)
            }
        }
}

class MainActivity : Activity() {

    private val disposable = CompositeDisposable()

    /** Обновляет UI в зависимости от текущего состояния */
    private fun view(model: Model) {
        progress.visibility = if (model.isFlight) View.VISIBLE else View.GONE
        results.text = model.searchResults.joinToString(separator = "\n")
        edit.error = model.error
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Observable
            .merge(
                RxView.clicks(google).map { SearchGoogle(edit.text.toString()) },
                RxView.clicks(yandex).map { SearchYandex(edit.text.toString()) })
            .publish { shared ->
                shared.flatMap { StateManager.createObservable(it).wrapToResult() }
            }
            .observeOn(Schedulers.from(::runOnUiThread))
            .scan(StateManager.init, StateManager::update)
            .subscribe(::view)
            .let(disposable::add)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}