package y2k.rxstateexample

import android.app.Activity
import android.os.Bundle
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge
import y2k.litho.elmish.experimental.*
import y2k.rxstateexample.Screen.Model
import y2k.rxstateexample.Screen.Msg
import y2k.rxstateexample.Screen.Msg.*
import y2k.rxstateexample.common.Services
import y2k.rxstateexample.common.Services.SearchEngine
import y2k.rxstateexample.common.Services.SearchEngine.Google
import y2k.rxstateexample.common.Services.SearchEngine.Yandex
import y2k.rxstateexample.common.button
import y2k.rxstateexample.common.editTextWithLabel
import y2k.rxstateexample.common.fullscreenProgress

class Screen : ElmFunctions<Model, Msg> {

    data class Model(
        val query: String = "",
        val isFlight: Boolean = false,
        val searchResults: List<String> = emptyList(),
        val error: String? = null)

    sealed class Msg {
        class QueryChanged(val query: String) : Msg()
        class SearchRequest(val engine: SearchEngine) : Msg()
        class SearchSuccess(val result: List<String>) : Msg()
        class SearchFailed(val result: Exception) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> = Model() to Cmd.none()

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> =
        when (msg) {
            is QueryChanged ->
                model.copy(query = msg.query) to Cmd.none()
            is SearchRequest ->
                model.copy(isFlight = true) to Cmd.fromContext(
                    { Services.search2(model.query, msg.engine) }, ::SearchSuccess, ::SearchFailed)
            is SearchSuccess ->
                model.copy(isFlight = false, searchResults = msg.result, error = null) to Cmd.none()
            is SearchFailed ->
                model.copy(isFlight = false, error = msg.result.message) to Cmd.none()
        }

    override fun ContainerBuilder.view(model: Model) {
        column {
            paddingDip(YogaEdge.ALL, 4f)

            editTextWithLabel(error = model.error) {
                hint("Search query")
                onTextChanged(::QueryChanged)
            }

            button {
                text("Search in Google")
                onClick(SearchRequest(Google))
            }
            button {
                text("Search in Yandex")
                onClick(SearchRequest(Yandex))
            }

            text {
                textSizeSp(20f)
                text(model.searchResults.joinToString(separator = "\n"))
            }
        }

        if (model.isFlight)
            fullscreenProgress()
    }
}

class ElmExampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program<Screen>()
    }
}