package culazy.app.hexboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.KeyEvent
import androidx.core.content.res.ResourcesCompat

//import com.squareup.moshi.Moshi

class KeyLayout {
    val groups: List<KeyGroup>
    private var currentLayerCode: Int? = null
    private var tempLayerCode: Int? = null
    private var keyCode: Int? = null
    private var keyDown = false
    private var layerTapStartAt: Long? = null
    private var maxWidth = 0
    private var inputService: InputService? = null
    private val keyHeight = 1.14927f
    private val heightOffset = 0.8660254f
    private val layerCodeMask = 0x10
    private val tapTimeMillis = 300
    private val charHistoryMax = 2
    private var charHistory = ArrayDeque<Int>(charHistoryMax)
    private val statusPaint = Paint()

    constructor(context: Context) {
        // TODO: Don't hard-code the layout, read it in from a file with (probably) moshi
        val mIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.hex_key, null)!!
        val mCenterIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.hex_key_center, null)!!
        statusPaint.typeface = Typeface.MONOSPACE
        statusPaint.color = Color.WHITE
        groups = listOf(
            KeyGroup(listOf(
                HexagonKey(0.5f, 0f, 1f, keyHeight, mIcon, 7 + layerCodeMask, "7", mCenterIcon),
                HexagonKey(1.5f, 0f, 1f, keyHeight, mIcon, 6 + layerCodeMask, "6", mCenterIcon),
                HexagonKey(2.5f, 0f, 1f, keyHeight, mIcon, 5 + layerCodeMask, "5", mCenterIcon),
                HexagonKey(3.5f, 0f, 1f, keyHeight, mIcon, 12 + layerCodeMask, "C", mCenterIcon),

                HexagonKey(0f, heightOffset, 1f, keyHeight, mIcon, 8 + layerCodeMask, "8", mCenterIcon),
                HexagonKey(1f, heightOffset, 1f, keyHeight, mIcon, 1 + layerCodeMask, "1", mCenterIcon),
                HexagonKey(2f, heightOffset, 1f, keyHeight, mIcon, 0 + layerCodeMask, "0", mCenterIcon),
                HexagonKey(3f, heightOffset, 1f, keyHeight, mIcon, 4 + layerCodeMask, "4", mCenterIcon),

                HexagonKey(0.5f, 2 * heightOffset, 1f, keyHeight, mIcon, 9 + layerCodeMask, "9", mCenterIcon),
                HexagonKey(1.5f, 2 * heightOffset, 1f, keyHeight, mIcon, 2 + layerCodeMask, "2", mCenterIcon),
                HexagonKey(2.5f, 2 * heightOffset, 1f, keyHeight, mIcon, 3 + layerCodeMask, "3", mCenterIcon),

                HexagonKey(0f, 3 * heightOffset, 1f, keyHeight, mIcon, 13 + layerCodeMask, "D", mCenterIcon),
                HexagonKey(1f, 3 * heightOffset, 1f, keyHeight, mIcon, 10 + layerCodeMask, "A", mCenterIcon),
                HexagonKey(2f, 3 * heightOffset, 1f, keyHeight, mIcon, 11 + layerCodeMask, "B", mCenterIcon),

                HexagonKey(0.5f, 4 * heightOffset, 1f, keyHeight, mIcon, 14 + layerCodeMask, "E", mCenterIcon),
                HexagonKey(1.5f, 4 * heightOffset, 1f, keyHeight, mIcon, 15 + layerCodeMask, "F", mCenterIcon),

                // TODO: add two extra keys for settings etc.
//                HexagonKey(0f, 5 * heightOffset, 1f, keyHeight, mIcon, 0, "", mCenterIcon),
//                HexagonKey(1f, 5 * heightOffset, 1f, keyHeight, mIcon, 0, "", mCenterIcon),
            )), KeyGroup(listOf(
//                HexagonKey(5f, 0.333f * heightOffset, 1f, keyHeight, mIcon, 0, ""),
//                HexagonKey(6f, 0.333f * heightOffset, 1f, keyHeight, mIcon, 0, ""),

                HexagonKey(4.5f, 1.333f * heightOffset, 1f, keyHeight, mIcon, 15, "F", mCenterIcon),
                HexagonKey(5.5f, 1.333f * heightOffset, 1f, keyHeight, mIcon, 14, "E", mCenterIcon),

                HexagonKey(4f, 2.333f * heightOffset, 1f, keyHeight, mIcon, 11, "B", mCenterIcon),
                HexagonKey(5f, 2.333f * heightOffset, 1f, keyHeight, mIcon, 10, "A", mCenterIcon),
                HexagonKey(6f, 2.333f * heightOffset, 1f, keyHeight, mIcon, 13, "D", mCenterIcon),

                HexagonKey(3.5f, 3.333f * heightOffset, 1f, keyHeight, mIcon, 3, "3", mCenterIcon),
                HexagonKey(4.5f, 3.333f * heightOffset, 1f, keyHeight, mIcon, 2, "2", mCenterIcon),
                HexagonKey(5.5f, 3.333f * heightOffset, 1f, keyHeight, mIcon, 9, "9", mCenterIcon),

                HexagonKey(3f, 4.333f * heightOffset, 1f, keyHeight, mIcon, 4, "4", mCenterIcon),
                HexagonKey(4f, 4.333f * heightOffset, 1f, keyHeight, mIcon, 0, "0", mCenterIcon),
                HexagonKey(5f, 4.333f * heightOffset, 1f, keyHeight, mIcon, 1, "1", mCenterIcon),
                HexagonKey(6f, 4.333f * heightOffset, 1f, keyHeight, mIcon, 8, "8", mCenterIcon),

                HexagonKey(2.5f, 5.333f * heightOffset, 1f, keyHeight, mIcon, 12, "C", mCenterIcon),
                HexagonKey(3.5f, 5.333f * heightOffset, 1f, keyHeight, mIcon, 5, "5", mCenterIcon),
                HexagonKey(4.5f, 5.333f * heightOffset, 1f, keyHeight, mIcon, 6, "6", mCenterIcon),
                HexagonKey(5.5f, 5.333f * heightOffset, 1f, keyHeight, mIcon, 7, "7", mCenterIcon),
            )),
        )
    }

    fun setInputService(input: InputService) {
        inputService = input
    }

    fun reset() {
        currentLayerCode = null
        tempLayerCode = null
        keyCode = null
        keyDown = false
        layerTapStartAt = null
        charHistory.clear()

        for (group in groups) {
            for (key in group.keys) {
                key.down = false
                key.enterTimeMillis = null
            }
        }
        updateKeyState()
    }

    fun onDraw(canvas: Canvas) {
        for (group in groups) {
            group.onDraw(canvas)
        }

        // draw character history
        var status = ""
        var status2 = ""
        for (code in charHistory) {
            status += "  " + code.toChar().toString()
            status2 += " " + (code shr 4).toString(16).uppercase() + (code and 0xF).toString(16).uppercase()
        }
        val layerCode = currentLayerCode ?: tempLayerCode
        if (layerCode != null) status2 += " " + layerCode.toString(16).uppercase()
        val multiplier = maxWidth / 7f
        statusPaint.textSize = multiplier * 0.3f
        canvas.drawText(status, 5 * multiplier, 0.25f * multiplier, statusPaint)
        canvas.drawText(status2, 5 * multiplier, 0.75f * multiplier, statusPaint)
    }

    fun setMaxWidth(width: Int) {
        maxWidth = width
        for (group in groups) {
            group.setMaxWidth(width)
        }
    }

    fun getHeight(): Int {
        var height = maxWidth / 2
        for (group in groups) {
            height = height.coerceAtLeast(group.getHeight())
        }
        return height
    }

    fun onDown(key: Key, timeMillis: Long) {
        if (key.code and layerCodeMask != 0 && !keyDown) {
            layerTapStartAt = timeMillis
        }
        onEnter(key, timeMillis)
    }

    fun onEnter(key: Key, timeMillis: Long) {
        key.down = true
        key.enterTimeMillis = timeMillis
        if (key.code and layerCodeMask == 0) {
            keyCode = key.code
            keyDown = true
            layerTapStartAt = null
        } else {
            currentLayerCode = key.code and 0xF
            tempLayerCode = null
            updateKeyState()
        }
    }

    fun onUp(key: Key, timeMillis: Long) {
        key.down = false
        key.enterTimeMillis = null
        if (key.code and layerCodeMask == 0) {
            val layerCode = currentLayerCode ?: tempLayerCode
            if (layerCode == null) {
                tempLayerCode = key.code
                keyDown = false
                updateKeyState()
                return
            }
            val fullCode = layerCode * 16 + key.code
            sendCharacter(fullCode)
            tempLayerCode = null
            keyCode = null
            keyDown = false
            updateKeyState()
        } else {
            currentLayerCode = null
            if (keyDown || (timeMillis - (layerTapStartAt ?: 0) <= tapTimeMillis)) {
                tempLayerCode = key.code and 0xF
            }
            updateKeyState()
        }
    }

    fun onLeave(key: Key, timeMillis: Long) {
        key.down = false
        key.enterTimeMillis = null
        if (key.code and layerCodeMask == 0) {
            keyDown = false
        } else {
            currentLayerCode = null
            layerTapStartAt = null
            updateKeyState()
        }
    }

    private fun sendCharacter(charCode: Int) {
        val connection = inputService?.currentInputConnection ?: return
        if (charHistory.count() >= charHistoryMax) charHistory.removeFirst()
        charHistory.addLast(charCode)
        when (charCode) {
            0 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_ENTER)
            1 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_PAGE_UP)
            2 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_MOVE_END)
            3 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_MOVE_HOME)
            4 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_PAGE_DOWN)
            5 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_COPY)
            6 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_PASTE)
            8 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_DEL)
            9 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_TAB)
//            10 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_ENTER)
            14 -> Input.sendCombo(connection, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_Z)
            15 -> Input.sendCombo(connection, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_Z)
            16 -> connection.performContextMenuAction(android.R.id.selectAll)
            17 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_DPAD_UP)
            18 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_DPAD_RIGHT)
            19 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_DPAD_LEFT)
            20 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_DPAD_DOWN)
            27 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_ESCAPE)
            30 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_FORWARD)
            31 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_BACK)
            127 -> Input.sendDownUp(connection, KeyEvent.KEYCODE_FORWARD_DEL)
            else -> connection.commitText(charCode.toChar().toString(), 1)
        }
    }

    private fun updateKeyState() {
        for (group in groups) {
            for (key in group.keys) {
                val layer = key.code and layerCodeMask != 0
                if (layer) {
                    key.active = key.code and 0xF == tempLayerCode
                } else {
                    val layerCode = currentLayerCode ?: tempLayerCode
                    key.secondaryLabel =
                        if (layerCode == null) null else getKeyLabel(layerCode * 16 + key.code)
                }
            }
        }
    }

    private fun getKeyLabel(fullCode: Int): String {
        return when(fullCode) {
            0 -> "⎆"
            1 -> "↟"
            2 -> "⇥"
            3 -> "⇤"
            4 -> "↡"
            5 -> "⎗" // 🗐🖆🖃✉🖂📩📝📄📋🗏
            6 -> "⎘"
            8 -> "⌫"
            9 -> "↹" // ⇥⭲↹
            10 -> "↵" // ⏎↵
            14 -> "↻" // ↪⥁↻
            15 -> "↺" // ↩⥀↺
            16 -> "⇱⇲"
            17 -> "↑"
            18 -> "→"
            19 -> "←"
            20 -> "↓"
            27 -> "␛" // 🗙␛
            30 -> "⇨" // ⤻⮊⮫⮳🢖⇨
            31 -> "⇦" // ⤺⮈⮪⮲🢔⇦
            32 -> "___" // ▁▁▁───
            in 33..126 -> fullCode.toChar().toString()
            127 -> "⌦"
            in 128..255 -> fullCode.toChar().toString()
            else -> "" // ✖❌
        }
    }
}
