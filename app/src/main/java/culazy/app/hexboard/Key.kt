package culazy.app.hexboard

import android.graphics.Canvas
import android.graphics.RectF

interface Key {

    val code: Int
    val bounds: RectF
    var down: Boolean
    var enterTimeMillis: Long?
    var active: Boolean
    var label: String?
    var secondaryLabel: String?

    fun onDraw(canvas: Canvas)

    fun resize(pixelsPerUnit: Float)

    /**
     * 1 means it's a perfectly centered touch, 0 or less means it's no touch at all.
     */
    fun percentUnderTouch(touch: KeyboardView.TouchPointData): Float
}
