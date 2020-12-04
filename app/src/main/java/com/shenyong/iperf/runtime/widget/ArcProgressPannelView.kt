package com.shenyong.iperf.runtime.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import com.shenyong.iperf.runtime.R
import java.text.DecimalFormat


/**
 *
 * @author ShenYong
 * @date 2019/7/3
 */

// 135
private const val ARC_START_ANGLE = 135f
// 270
private const val SWEEP_ANGLE_MAX = 270f
private const val STEP_COUNT = 50
/** 到最大量程的格数 */
private const val RANGE_SIZE = 10

class ArcProgressPannelView : View {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arcW = 0f
    private lateinit var barShader: SweepGradient
    private var barColors: Array<Int>
    private var arcRect = RectF()
    /** 长刻度线 */
    private var minorScaleBarH: Int
    /** 短刻度线 */
    private var majorScaleBarH: Int
    /** 刻度颜色 */
    private var scaleBarColor: Int
    private var scaleBarW: Int
    private var textOffset: Float
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var maxRange = 100f
    private var rangeStep = maxRange / RANGE_SIZE
    /** 当前值 */
    private var curValue = 0f
    private var curPecent = 0f
    private var drawPecent = 0f
    private var pointer: Bitmap? = null
    private var decimalFormat = DecimalFormat("0.0")
    private lateinit var valueAnimator: ValueAnimator

    init {
        arcW = resources.getDimensionPixelSize(R.dimen.arc_progress_w).toFloat()
        barColors = arrayOf(resources.getColor(R.color.arc_bar_1),
            resources.getColor(R.color.arc_bar_2),
            resources.getColor(R.color.arc_bar_3),
            resources.getColor(R.color.arc_bar_4),
            resources.getColor(R.color.arc_bar_5),
            resources.getColor(R.color.arc_bar_6),
            Color.WHITE,
            Color.WHITE)
        majorScaleBarH = resources.getDimensionPixelSize(R.dimen.major_scale_bar_h)
        minorScaleBarH = resources.getDimensionPixelSize(R.dimen.minor_scale_bar_h)
        scaleBarColor = resources.getColor(R.color.arc_scale_bar_color)
        scaleBarW = resources.getDimensionPixelSize(R.dimen.scale_bar_w)
        val textSize = resources.getDimension(R.dimen.scale_text_size)
        textOffset = resources.getDimension(R.dimen.scale_text_offset)
        textPaint.color = scaleBarColor
        textPaint.textSize = textSize
        pointer = BitmapFactory.decodeResource(resources, R.drawable.img_pointer)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (pointer == null) {
            pointer = BitmapFactory.decodeResource(resources, R.drawable.img_pointer)
        }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (pointer != null) {
            pointer?.recycle()
            pointer = null
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = w / 2 + w / 2 * Math.cos(Math.toRadians(45.0))
        setMeasuredDimension(w, h.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val offset = arcW / 2
        barShader = SweepGradient((w / 2).toFloat(), (w / 2).toFloat(), barColors.toIntArray(), null)
        arcRect = RectF(offset, offset, w - offset, w - offset)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        canvas.save()
        canvas.rotate(ARC_START_ANGLE, (width / 2).toFloat(), (width / 2).toFloat())
        paint.shader = barShader
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = arcW
        canvas.drawArc(arcRect, 0f, SWEEP_ANGLE_MAX, false, paint)
        paint.shader = null
        canvas.restore()

        canvas.save()

        canvas.translate((width / 2).toFloat(), (width / 2).toFloat())
        val totalOffsetAngle = 2f
        val innerR = width / 2 - arcW
        paint.color = scaleBarColor
        paint.strokeWidth = scaleBarW.toFloat()
        canvas.save()
        /** 绘制当前值指针 */
        // 旋转270°指针正好向上
        val angle = ARC_START_ANGLE + (SWEEP_ANGLE_MAX - totalOffsetAngle) * this.drawPecent + totalOffsetAngle / 2
        canvas.rotate(angle)
        val maxLen = innerR - majorScaleBarH - textOffset
        pointer?.let {
            canvas.scale(maxLen / it.width, 1f)
            val startX = it.height / 2f
            canvas.drawBitmap(it, 0f, -startX, paint)
        }
        canvas.restore()

        /** 绘制刻度 */
        var rotateTotal = ARC_START_ANGLE + totalOffsetAngle / 2
        canvas.rotate(rotateTotal)
        val step = (SWEEP_ANGLE_MAX - totalOffsetAngle) / STEP_COUNT
        var value = 0f
        for (i in 0 until STEP_COUNT + 1) {
            if (i == 0 || i % 5 == 0) {
                canvas.drawLine(innerR, 0f, innerR - majorScaleBarH, 0f, paint)
                if (i % 5 == 0) {
                    val text = decimalFormat.format(value)
                    value += rangeStep
                    val textLen = textPaint.measureText(text)
                    val middle = getTextMiddle() / 2
                    canvas.save()
                    canvas.translate(innerR - majorScaleBarH - textOffset - textLen / 2, 0f)
                    canvas.rotate(-rotateTotal)
                    canvas.drawText(text, -textLen / 2, middle, textPaint)
                    canvas.restore()
                }
            } else {
                canvas.drawLine(innerR, 0f, innerR - minorScaleBarH, 0f, paint)
            }
            rotateTotal += step
            canvas.rotate(step)
        }
        canvas.restore()
    }

    private fun getTextMiddle(): Float {
        val fm = textPaint.fontMetrics
        return (Math.abs(fm.top) + fm.bottom) / 2
    }

    fun setArcBarColors(vararg colors: Int) {
        barColors = colors.toTypedArray()
        invalidate()
    }

    /** 设置最大量程 */
    fun setMaxRange(maxRange: Float) {
        this.maxRange = maxRange
        rangeStep = maxRange / RANGE_SIZE
        invalidate()
    }

    fun setCurrentVal(value: Float) {
        curValue = if (value > maxRange) maxRange else value
        curValue = if (value < 0) 0f else curValue
        curPecent = curValue * 1f / maxRange

        if (::valueAnimator.isInitialized) {
            valueAnimator.removeAllUpdateListeners()
            valueAnimator.cancel()
        }
        valueAnimator = ValueAnimator.ofFloat(this.drawPecent, curPecent)
        valueAnimator.duration = 1000
        valueAnimator.interpolator = OvershootInterpolator()
        valueAnimator.addUpdateListener { animation ->
            this.drawPecent = animation.animatedValue as Float
            if (this.drawPecent > maxRange) {
                this.drawPecent = maxRange
            }
            if (this.drawPecent < 0) {
                this.drawPecent = 0f
            }
            invalidate()
        }
        valueAnimator.start()
    }
}