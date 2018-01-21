package y2k.rxstateexample.common

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.soloader.SoLoader
import io.reactivex.Observable
import kotlinx.coroutines.experimental.delay
import y2k.rxstateexample.common.Result.*
import java.util.concurrent.TimeUnit

object Services {

    /** Поисковые движки */
    enum class SearchEngine { Google, Yandex }

    /**
     * Выполнить поиск на выбронном поисковом вдижке
     * @param query Текст запроса
     * @param engine Поисковый движок
     * @return Список результатов
     */
    fun search(query: String, engine: SearchEngine): Observable<List<String>> =
        Observable
            .fromCallable {
                // XXX: в 30% случаев падаем с исключением
                if (Math.random() < 0.3) throw Exception("(Test) Network Error")
                // XXX: фейковые данные
                List((Math.random() * 32).toInt()) {
                    "$engine ($query) #" + (10000 * Math.random()).toInt()
                }
            }
            .delay((500 + Math.random() * 1500).toLong(), TimeUnit.MILLISECONDS)

    suspend fun search2(query: String, engine: SearchEngine): List<String> {
        delay((500 + Math.random() * 1500).toLong())
        // XXX: в 30% случаев падаем с исключением
        if (Math.random() < 0.3) throw Exception("Network Error (test)")
        // XXX: фейковые данные
        return List((Math.random() * 16).toInt()) {
            "$engine ($query) #" + (10000 * Math.random()).toInt()
        }
    }
}

fun <R> Observable<R>.toResult(): Observable<Result<R, Throwable>> =
    map<Result<R, Throwable>> { Success(it) }
        .onErrorReturn { error -> Failure(error) }
        .startWith(InFlight)

sealed class Result<out T, out E> {
    object InFlight : Result<Nothing, Nothing>()
    class Success<out T>(val value: T) : Result<T, Nothing>()
    class Failure<out E>(val error: E) : Result<Nothing, E>()
}

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        SoLoader.init(this, false)
    }
}