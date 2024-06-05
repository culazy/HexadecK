package culazy.app.hexboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo

class InputService : InputMethodService(), android.inputmethodservice.KeyboardView.OnKeyboardActionListener {

    // TODO: remove old original implementation, eventually
    private val originalPrototype = false
    private var priorCode: Int? = null
    private var lastWasSecondary = false
    private var keyboard: Keyboard? = null
    private var view: android.inputmethodservice.KeyboardView? = null
    private var lastFullCode: Int? = null
    private var keyboardView: KeyboardView? = null

    override fun onCreateInputView(): View {
        if (originalPrototype) {
            priorCode = null
            view = layoutInflater.inflate(R.layout.keyboard_layout_original, null) as android.inputmethodservice.KeyboardView
            keyboard = Keyboard(this, R.xml.keyboard)
            view!!.keyboard = keyboard
            view!!.setOnKeyboardActionListener(this)
            updateKeyLabels()
            return view!!
        } else {
            val keyboardView = layoutInflater.inflate(R.layout.keyboard_layout, null) as culazy.app.hexboard.KeyboardView
            keyboardView.setMaxWidth(this.maxWidth)
            keyboardView.setInputService(this)
            this.keyboardView = keyboardView
            return keyboardView
        }
    }

    override fun onPress(primaryCode: Int) {
        when (primaryCode) {
            -2 -> {
                if (priorCode == null && lastFullCode != null) {
                    applyFullCode(lastFullCode!!)
                }
            }
            -3 -> currentInputConnection.commitText(" ", 1)
            else -> {
                val isSecondary = primaryCode and 0x80 != 0
                val code = primaryCode and 0xF
                applyText(code, isSecondary)
            }
        }
    }

    override fun onRelease(primaryCode: Int) {
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
    }

    override fun onText(text: CharSequence?) {
    }

    private fun applyText(code: Int, isSecondary: Boolean) {
        var secondaryCode = priorCode
        if (secondaryCode == null) {
            // if we don't have a prior code, we need to store off this one and wait for another keypress
            priorCode = code
            lastWasSecondary = isSecondary
            updateKeyLabels()
            return
        }
        var primaryCode = code
        if (isSecondary) {
            if (lastWasSecondary) {
                if (code == secondaryCode) {
                    // pressing a secondary key again will undo it
                    priorCode = null
                } else {
                    // pressing a different secondary key will override the current one
                    priorCode = code
                }
                updateKeyLabels()
                return
            }
            // if a secondary key is pressed after a primary key, assume they were just typed out of order
            primaryCode = secondaryCode
            secondaryCode = code
        }

        // calculate full key code and apply it
        val fullCode = secondaryCode * 16 + primaryCode
        priorCode = null
        applyFullCode(fullCode)
        lastFullCode = fullCode
        updateKeyLabels()
    }

    private fun applyFullCode(fullCode: Int) {
        when (fullCode) {
            8 -> Input.sendDownUp(currentInputConnection, KeyEvent.KEYCODE_DEL)
            10 -> Input.sendDownUp(currentInputConnection, KeyEvent.KEYCODE_ENTER)
            127 -> Input.sendDownUp(currentInputConnection, KeyEvent.KEYCODE_FORWARD_DEL)
            else -> currentInputConnection.commitText(fullCode.toChar().toString(), 1)
        }
    }

    override fun swipeLeft() {
    }

    override fun swipeRight() {
    }

    override fun swipeDown() {
    }

    override fun swipeUp() {
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        val view = keyboardView ?: return
        view.reset()

    }

    private fun updateKeyLabels() {
        for (key in keyboard!!.keys) {
            updateKeyLabel(key)
        }
        view!!.invalidateAllKeys()
    }

    private fun updateKeyLabel(key: Keyboard.Key) {
        val primaryCode = key.codes[0]
        if (primaryCode < 0) {
            if (primaryCode === -2) {
                if (priorCode != null) {
                    key.label = priorCode!!.toString(16).uppercase() + ' '
                } else if (lastFullCode == null) {
                    key.label = ""
                } else {
                    key.label = lastFullCode!!.toString(16).uppercase().padStart(2, '0')
                }
            }
            return
        }

        val isSecondary = primaryCode and 0x80 != 0
        val code = primaryCode and 0xF
        val codeLabel = if (primaryCode < 0) "" else code.toString(16).uppercase()
        if (isSecondary) {
            if (priorCode == null) {
                key.label = " $codeLabel "
            } else {
                if (lastWasSecondary) {
                    if (priorCode == code) {
                        key.label = "✖"
                    } else {
                        key.label = " $codeLabel "
                    }
                } else {
                    val fullCode = priorCode!! + code * 16
                    key.label = getKeyLabel(fullCode)
                }
            }
            return
        }

        if (priorCode == null) {
            key.label = " $codeLabel "
            return
        }
        val fullCode = priorCode!! * 16 + code
        key.label = getKeyLabel(fullCode)
    }

    private fun getKeyLabel(fullCode: Int): CharSequence {
        return when(fullCode) {
            8 -> "⌫"
            9 -> "↹" // ⇥⭲↹
            10 -> "↵" // ⏎↵
            27 -> "ESC"
            in 32..126 -> fullCode.toChar().toString()
            127 -> "⌦"
            else -> "-✖-" // ✖❌
        }
    }
}
