package photoeditor.photoart.onetap.beziertest.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * Created by Yue on 2021/4/1.
 */

const val TYPE_CTRL1 = 100
const val TYPE_CTRL2 = 101


//根据两个控制点绘制贝塞尔曲线
class BezierView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var type = 0

    //起始点 结束点 固定在左下角和右上角
    private val start = PointF()
    private val end = PointF()

    //控制点的坐标
    val ctrl1 = PointF()
    val ctrl2 = PointF()

    //贝塞尔曲线的路径
    private val path = Path()


    //控制点的画笔
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    //贝塞尔曲线的画笔
    private val bezierPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    //辅助线的画笔
    private val helpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    //文字的画笔
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        textSize = 32f
        style = Paint.Style.FILL_AND_STROKE
    }




    //触摸手势
    private val gesDet =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                type = getType(e.x, e.y)
                return (type == TYPE_CTRL1 || type == TYPE_CTRL2)
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val point = when (type) {
                    TYPE_CTRL1 -> ctrl1
                    TYPE_CTRL2 -> ctrl2
                    else -> null
                }

                if (e2.x < 0 || e2.x > measuredWidth || e2.y < 0 || e2.y > measuredHeight) return super.onScroll(
                    e1,
                    e2,
                    distanceX,
                    distanceY
                )

                return point?.let {
                    it.x = e2.x
                    it.y = e2.y
                    invalidate()
                    true
                } ?: super.onScroll(e1, e2, distanceX, distanceY)

            }

        })

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        val b = if (event == null || event.pointerCount > 1)
            super.onTouchEvent(event)
        else
            gesDet.onTouchEvent(event)
        if (b) performClick()
        return b
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //初始化各个点的坐标
        start.x = 0f
        start.y = h.toFloat()

        end.x = w.toFloat()
        end.y = 0f

        ctrl1.x = w / 2f - 100
        ctrl1.y = 100f

        ctrl2.x = w / 2f + 100
        ctrl2.y = h - 100f

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        //绘制辅助线
        canvas.drawLine(start.x, start.y, ctrl1.x, ctrl1.y, helpPaint)
        canvas.drawLine(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, helpPaint)
        canvas.drawLine(ctrl2.x, ctrl2.y, end.x, end.y, helpPaint)

        //绘制贝塞尔
        path.reset()
        path.moveTo(start.x, start.y)
        path.cubicTo(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, end.x, end.y)
        canvas.drawPath(path, bezierPaint)

        //绘制点
        canvas.drawPoint(ctrl1.x, ctrl1.y, pointPaint)
        canvas.drawPoint(ctrl2.x, ctrl2.y, pointPaint)

        val p1x = "%.2f".format(ctrl1.x / measuredWidth)
        val p1y = "%.2f".format(1 - ctrl1.y / measuredHeight)
        val p2x = "%.2f".format(ctrl2.x / measuredWidth)
        val p2y = "%.2f".format(1 - ctrl2.y / measuredHeight)

        //绘制坐标值
        canvas.drawText("( X: $p1x  Y: $p1y )", ctrl1.x - 128f, ctrl1.y - 48f, textPaint)
        canvas.drawText("( X: $p2x  Y: $p2y )", ctrl2.x - 128f, ctrl2.y - 48f, textPaint)
    }

    //判断落点
    private fun getType(x: Float, y: Float) = when {
        abs(x - ctrl1.x) < 100 && abs(y - ctrl1.y) < 100 -> TYPE_CTRL1
        abs(x - ctrl2.x) < 100 && abs(y - ctrl2.y) < 100 -> TYPE_CTRL2
        else -> 0
    }


}