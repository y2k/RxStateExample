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
import y2k.rxstateexample.Services.SearchEngine
import y2k.rxstateexample.Services.SearchEngine.Google
import y2k.rxstateexample.Services.SearchEngine.Yandex
import y2k.rxstateexample.StateManager.Event.SearchRequest
import y2k.rxstateexample.StateManager.Event.SearchResult
import y2k.rxstateexample.StateManager.Model

object StateManager {

    /** Стейт всего окна */
    data class Model(
        /** Поиск в процессе (те если true, то надо показывать прогресс-бар) */
        val isFlight: Boolean,
        /** Результаты поиска */
        val searchResults: List<String>,
        /** Ошибка последнего запроса */
        val error: String?)

    /** События */
    sealed class Event {
        /** Событие "пользователь нажал 'искать' с заданными параметрами" */
        class SearchRequest(val query: String, val engine: SearchEngine) : Event()

        /** Событие "результат поиска" */
        class SearchResult(val result: Result<List<String>, Throwable>) : Event()
    }

    /** Начальное состояние окна */
    val init = Model(false, emptyList(), null)

    /** Создать асинхронный запрос по заданному эвенту */
    fun createObservable(event: Event): Observable<Event> =
        when (event) {
            is SearchRequest ->
                Services.search(event.query, event.engine).toResult().map(::SearchResult)
            else -> Observable.never()
        }

    /** Обновляет состояние программы в зависимости от евентов */
    fun update(model: Model, event: Event): Model =
        when (event) {
            is SearchResult -> {
                val result = event.result
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
            else -> model
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
                RxView.clicks(google).map { SearchRequest(edit.text.toString(), Google) },
                RxView.clicks(yandex).map { SearchRequest(edit.text.toString(), Yandex) })
            .publish { shared ->
                shared.flatMap { StateManager.createObservable(it) }
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