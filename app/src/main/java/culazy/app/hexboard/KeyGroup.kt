package culazy.app.hexboard

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.roundToInt

class KeyGroup(keys: List<Key>) {
    val keys = HashSet<Key>()
    private val horizontalKeyCount = 7
    private var mBoundsInvalidated = true
    private var keySize = 0f
    private val keyLabelPaint = Paint()
    private val keySecondaryLabelPaint = Paint()

    init {
        this.keys.addAll(keys)
        keyLabelPaint.color = Color.WHITE
        keyLabelPaint.textAlign = Paint.Align.CENTER
        keySecondaryLabelPaint.color = Color.YELLOW
        keySecondaryLabelPaint.textAlign = Paint.Align.CENTER
    }

    fun onDraw(canvas: Canvas) {
        for (key in keys) {
            if (mBoundsInvalidated) {
                key.resize(keySize)
            }
            key.onDraw(canvas)
        }
        mBoundsInvalidated = false
    }

    fun setMaxWidth(width: Int) {
        keySize = width.toFloat() / horizontalKeyCount
        keyLabelPaint.textSize = keySize * 0.4f
        keySecondaryLabelPaint.textSize = keySize * 0.3f
    }

    fun getHeight(): Int {
        var height = 0
        for(key in keys) {
            // TODO: fix this calculation, currently working off of the wrong value kinda
            height = height.coerceAtLeast(getPixelPos(key.bounds.bottom))
        }
        return height
    }

    /**
     * Converts a key-increment location to a pixel location
     */
    private fun getPixelPos(pos: Float): Int {
        return (pos * keySize).roundToInt()
    }
}
