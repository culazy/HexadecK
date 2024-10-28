package culazy.app.hexboard

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt
import kotlin.math.sqrt

class HexagonKey(
    posX: Float,
    posY: Float,
    width: Float = 1f,
    height: Float = 1f,
    private val icon: Drawable,
    override val code: Int,
    override var label: String?,
    val centerIcon: Drawable,
): Key {
    override val bounds: RectF
    override var down = false
    override var active = false
    override var enterTimeMillis: Long? = null
    override var secondaryLabel: String? = null
    object Theme {
        val passiveColor = Color.argb(192, 255, 255, 0)
        val activeColor = Color.argb(224, 255, 255, 224)
        val downColor = Color.argb(128, 128, 128, 128)
        val centerPassiveColor = Color.argb(112, 224, 96, 0)
        val centerActiveColor = Color.argb(224, 224, 96, 0)
        val centerDownColor = Color.argb(32, 128, 128, 128)
    }
    private val iconBounds = Rect()
    private val centerIconBounds = Rect()
    private val labelPaint = Paint()
    private val secondaryLabelPaint = Paint()
    private val dotPaint = Paint()
    private val inactiveDotPaint = Paint()

    init {
        bounds = RectF(posX, posY, posX + width, posY + height)
        labelPaint.color = Color.argb(255, 255, 192, 0)
        labelPaint.textAlign = Paint.Align.CENTER
        secondaryLabelPaint.color = Color.argb(255, 255, 255, 224)
        secondaryLabelPaint.textAlign = Paint.Align.CENTER
        dotPaint.color = Color.YELLOW
        inactiveDotPaint.color = Color.argb(80, 128, 192, 255)
    }

    /**
     * 1 means it's a perfectly centered touch, 0 or less means it's no touch at all.
     */
    override fun percentUnderTouch(touch: KeyboardView.TouchPointData): Float {
        val radius = Math.min(iconBounds.width(), iconBounds.height()) / 2f
        val xDiff = iconBounds.exactCenterX() - touch.x
        val yDiff = iconBounds.exactCenterY() - touch.y
        return 1 - sqrt(xDiff * xDiff + yDiff * yDiff) / radius
    }

    override fun resize(pixelsPerUnit: Float) {
        labelPaint.textSize = pixelsPerUnit * 0.4f
        secondaryLabelPaint.textSize = pixelsPerUnit * 0.3f
        iconBounds.set(
            (bounds.left * pixelsPerUnit).roundToInt(),
            (bounds.top * pixelsPerUnit).roundToInt(),
            (bounds.right * pixelsPerUnit).roundToInt(),
            (bounds.bottom * pixelsPerUnit).roundToInt(),
        )
        val centerX = iconBounds.centerX()
        val centerY = iconBounds.centerY()
        centerIconBounds.set(
            (centerX - iconBounds.width() * 0.4f).roundToInt(),
            (centerY - iconBounds.height() * 0.25f).roundToInt(),
            (centerX + iconBounds.width() * 0.4f).roundToInt(),
            (centerY + iconBounds.height() * 0.5f).roundToInt(),
        )
    }

    override fun onDraw(canvas: Canvas) {
        icon.bounds = iconBounds
        icon.setColorFilter(
            if (down) Theme.downColor
            else if (active) Theme.activeColor
                else Theme.passiveColor,
            PorterDuff.Mode.MULTIPLY
        )
        icon.draw(canvas)

        centerIcon.bounds = centerIconBounds
        centerIcon.setColorFilter(
            if (down) Theme.centerDownColor
            else if (active) Theme.centerActiveColor
                else Theme.centerPassiveColor,
            PorterDuff.Mode.MULTIPLY
        )
        centerIcon.draw(canvas)

        val centerX = iconBounds.exactCenterX()
        val centerY = iconBounds.exactCenterY()

        val dotOffsetX = iconBounds.width() * 0.45f * 0.8660254f
        val dotOffsetY = iconBounds.width() * 0.45f * 0.5f
        canvas.drawCircle(
            centerX - dotOffsetX,
            centerY - dotOffsetY,
            iconBounds.width() * 0.03f,
            if (code and 1 > 0) dotPaint else inactiveDotPaint,
        )
        canvas.drawCircle(
            centerX - dotOffsetX,
            centerY + dotOffsetY,
            iconBounds.width() * 0.03f,
            if (code and 2 > 0) dotPaint else inactiveDotPaint,
        )
        canvas.drawCircle(
            centerX + dotOffsetX,
            centerY - dotOffsetY,
            iconBounds.width() * 0.03f,
            if (code and 4 > 0) dotPaint else inactiveDotPaint,
        )
        canvas.drawCircle(
            centerX + dotOffsetX,
            centerY + dotOffsetY,
            iconBounds.width() * 0.03f,
            if (code and 8 > 0) dotPaint else inactiveDotPaint,
        )

        if (secondaryLabel != null) {
            canvas.drawText(
                secondaryLabel!!,
                centerX,
                centerY + iconBounds.height() * 0.25f,
                secondaryLabelPaint
            )
        }

        if (label != null) {
            canvas.drawText(
                label!!,
                centerX,
                centerY - iconBounds.height() * 0.05f,
                labelPaint
            )
        }
    }
}
