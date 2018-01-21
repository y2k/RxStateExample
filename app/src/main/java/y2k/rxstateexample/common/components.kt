package y2k.rxstateexample.common

import android.graphics.Color
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.widget.EditText
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaJustify
import com.facebook.yoga.YogaPositionType
import y2k.litho.elmish.experimental.column
import y2k.litho.elmish.experimental.editText
import y2k.litho.elmish.experimental.progress
import y2k.litho.elmish.experimental.text
import y2k.rxstateexample.R

fun ContainerBuilder.editTextWithLabel(
    error: String?, init: EditText.Builder.() -> Unit) {
    column {
        editText {
            textSizeSp(30f)
            isSingleLine(true)
            init()
        }
        text {
            textColor(Color.RED)
            textSizeSp(20f)
            text(error)
        }
    }
}

fun ContainerBuilder.fullscreenProgress() {
    column {
        backgroundRes(R.color.colorPrimary)
        positionType(YogaPositionType.ABSOLUTE)
        positionDip(YogaEdge.ALL, 0f)
        alignItems(YogaAlign.CENTER)
        justifyContent(YogaJustify.CENTER)

        progress {
            widthDip(100f)
            heightDip(100f)
        }
    }
}

fun ContainerBuilder.button(init: Text.Builder.() -> Unit) {
    text {
        marginDip(YogaEdge.ALL, 2f)
        paddingDip(YogaEdge.ALL, 4f)
        backgroundRes(R.drawable.button_simple)
        textSizeSp(30f)
        init()
    }
}