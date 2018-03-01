package y2k.rxstateexample.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import y2k.rxstateexample.R

inline fun EditText.onTextChanged(crossinline f: (String) -> Unit) {
    val oldWatcher = getTag(R.id.text_watcher) as? TextWatcher
    if (oldWatcher != null) removeTextChangedListener(oldWatcher)
    val watcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = f("" + s)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    }
    addTextChangedListener(watcher)
    setTag(R.id.text_watcher, watcher)
}