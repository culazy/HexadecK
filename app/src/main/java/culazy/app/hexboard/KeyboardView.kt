package culazy.app.hexboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import kotlin.math.roundToInt

fun Rect.set(left: Float, top: Float, right: Float, bottom: Float) {
    this.set(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
}

class KeyboardView : View {

    private val touchPointLifetime = 1

    private var maxWidth = 0
    private var layout: KeyLayout
    private var debugInfo = ""
    private var gestureCount = 0
    private var actionCount = 0

    private val debugPaint = Paint()
    private val touchLinePaint = Paint()
    private val touchPoints = HashMap<Int, TouchPointData>()
    private val vibrator: Vibrator?
    private val touchIcon: Drawable
    private val touchIconBounds = Rect()

    constructor(context: Context) : this(context, null, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        touchIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.soft_circle, null)!!
        layout = KeyLayout(context)
        // Get instance of Vibrator from current Context
        vibrator = getSystemService(context, Vibrator::class.java)
        debugPaint.color = Color.WHITE
        debugPaint.textSize = 60f
        touchLinePaint.color = Color.argb(96, 255, 192, 0)
        touchLinePaint.strokeWidth = 10f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TODO: get height correctly
        setMeasuredDimension(maxWidth, layout.getHeight())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.drawColor(Color.BLACK)

        // draw touch points
        for (entry in touchPoints) {
            if (entry.value.lastGestureIndex <= gestureCount - touchPointLifetime) {
                continue
            }
            canvas.drawLine(entry.value.downX, entry.value.downY, entry.value.x, entry.value.y, touchLinePaint)
            touchIconBounds.set(entry.value.downX - 40, entry.value.downY - 40, entry.value.downX + 40, entry.value.downY + 40)
            touchIcon.bounds = touchIconBounds
            touchIcon.setColorFilter(Color.argb(80, 255, 128, 0), PorterDuff.Mode.MULTIPLY)
            touchIcon.draw(canvas)
            touchIconBounds.set(entry.value.x - 50, entry.value.y - 50, entry.value.x + 50, entry.value.y + 50)
            touchIcon.bounds = touchIconBounds
            touchIcon.setColorFilter(Color.argb(112, 255, 192, 0), PorterDuff.Mode.MULTIPLY)
            touchIcon.draw(canvas)
        }
        layout.onDraw(canvas)
        canvas.drawText(debugInfo, 0f, 300f, debugPaint)
        canvas.restore()
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev!!.actionMasked
        val index = ev.actionIndex
        val id = ev.getPointerId(index)

        // Invalidate to request a redraw
        invalidate()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                gestureCount++
                val x = ev.getX(index)
                val y = ev.getY(index)
                val data = getTouchPointData(id, x, y)
                val key = getTouchKey(data)
                data.key = key
                data.down = true
                data.size = ev.size
                data.lastGestureIndex = gestureCount
                data.downX = x
                data.downY = y
                keyDown(key, ev.eventTime)
            }

            MotionEvent.ACTION_UP -> {
                val x = ev.getX(index)
                val y = ev.getY(index)
                val data = getTouchPointData(id, x, y)
                keyUp(data, ev.eventTime)
                data.down = false
                data.size = ev.size
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val x = ev.getX(index)
                val y = ev.getY(index)
                val data = getTouchPointData(id, x, y)
                val key = getTouchKey(data)
                data.key = key
                data.down = true
                data.size = ev.size
                data.lastGestureIndex = gestureCount
                data.downX = x
                data.downY = y
                keyDown(key, ev.eventTime)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val x = ev.getX(index)
                val y = ev.getY(index)
                val data = getTouchPointData(id, x, y)
                keyUp(data, ev.eventTime)
                data.down = false
                data.size = ev.size
                data.upX = x
                data.upY = y
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0..<ev.pointerCount) {
                    val pointerId = ev.getPointerId(i)
                    val x = ev.getX(i)
                    val y = ev.getY(i)
                    val data = getTouchPointData(pointerId, x, y)
                    data.size = ev.getSize(i)
                    val percentUnder = data.key?.percentUnderTouch(data) ?: -1f
                    if (percentUnder < -0.1) {
                        val newKey = getTouchKey(data)
                        if (data.key != null) layout.onLeave(data.key!!, ev.eventTime)
                        data.key = newKey
                        if (newKey != null) layout.onEnter(newKey, ev.eventTime)
                    }
                }
            }

            else -> {
                ++actionCount
                debugInfo = "other action: %d".format(action)
            }
        }
        return true
    }

    fun setMaxWidth(width: Int) {
        maxWidth = width
        layout.setMaxWidth(width)
    }

    fun setInputService(input: InputService) {
        layout.setInputService(input)
    }

    fun reset() {
        gestureCount = 0
        actionCount = 0
        touchPoints.clear()
        layout.reset()
        invalidate()
    }

    private fun keyDown(key: Key?, timeMillis: Long) {
        if (key == null) return
        layout.onDown(key, timeMillis)
    }

    private fun keyUp(touch: TouchPointData, timeMillis: Long) {
        val key = touch.key
        if (key == null) {
            vibrator?.vibrate(150)
            return
        }
        val percentUnder = key.percentUnderTouch(touch)
        if (percentUnder < 0.5) vibrator?.vibrate(50)
        layout.onUp(key, timeMillis)
    }

    private fun getTouchKey(touch: TouchPointData): Key? {
        for (group in layout.groups) {
            for (key in group.keys) {
                if (!key.down && key.percentUnderTouch(touch) > 0) {
                    return key
                }
            }
        }
        return null
    }

    private fun getTouchPointData(touchId: Int, x: Float, y: Float): TouchPointData {
        val data = touchPoints.getOrPut(touchId) {
            TouchPointData(x, y)
        }
        data.x = x
        data.y = y
        return data
    }

    class TouchPointData(
        var x: Float,
        var y: Float,
    ) {
        var down = true
        var downX = 0f
        var downY = 0f
        var upX = 0f
        var upY = 0f
        var size = 0f
        var lastGestureIndex = 0
        var key: Key? = null
    }
}
