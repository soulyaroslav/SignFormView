package soulyaroslav.library.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import soulyaroslav.library.R

/**
 * Created by yaroslav on 7/4/17.
 */
class FieldIconView : View {

    private lateinit var backgroundPaint: Paint
    private lateinit var backgroundStrokePaint: Paint
    private var bitmap: Bitmap? = null
    private var padding = 0
    private var strokeColor = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        initBackgroundPaint()
        initStrokeBackgroundPaint()
    }

    private fun initBackgroundPaint() {
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.apply {
            color = ContextCompat.getColor(context, R.color.background)
            style = Paint.Style.FILL
        }
    }

    private fun initStrokeBackgroundPaint() {
        backgroundStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundStrokePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width).toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewWidth = reconcileSize(View.MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec)
        val viewHeight = reconcileSize(View.MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawIcon(canvas)
    }

    private fun drawBackground(canvas: Canvas?) {
        val centerX = width * .5f
        val centerY = height * .5f
        val radius = Math.min(centerX, centerY) - padding
        canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)
        canvas?.drawCircle(centerX, centerY, radius, backgroundStrokePaint)
    }

    private fun drawIcon(canvas: Canvas?) {
        val centerX = width * .5f
        val centerY = height * .5f
        bitmap?.let {
            canvas?.drawBitmap(bitmap, centerX - bitmap!!.width / 2, centerY - bitmap!!.height / 2, null)
        }
    }

    private fun reconcileSize(contentSize: Int, measureSpec: Int): Int {
        val mode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        when (mode) {
            View.MeasureSpec.EXACTLY -> return specSize
            View.MeasureSpec.AT_MOST -> {
                if (contentSize < specSize) {
                    return contentSize
                } else {
                    return specSize
                }
            }
            View.MeasureSpec.UNSPECIFIED -> {
                return contentSize
            }
            else -> return contentSize
        }
    }

    fun setPadding(padding: Int) {
        this.padding = padding
    }

    fun setStrokeColor(strokeColor: Int) {
        this.strokeColor = strokeColor
        backgroundStrokePaint.color = ContextCompat.getColor(context, strokeColor)
        invalidate()
    }

    fun setFieldIcon(iconId: Int) {
        bitmap = BitmapFactory.decodeResource(resources, iconId)
    }
}