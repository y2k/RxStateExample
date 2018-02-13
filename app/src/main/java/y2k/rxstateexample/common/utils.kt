package y2k.rxstateexample.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

inline fun EditText.addTextChangedListener(crossinline f: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = f("" + s)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    })
}