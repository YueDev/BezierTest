package photoeditor.photoart.onetap.beziertest

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import kotlin.math.abs

/**
 * Created by Yue on 2021/12/17.
 */

class PenView : View {

    companion object {

        const val INVALIDATED_TOUCH_INDEX = -1
        const val TOUCH_AREA_SIZE = 96f
    }

    //点
    private val points = mutableListOf<BezierPoint>()

    //路径
    private val path = Path()

    //移动点的index
    private var touchIndex = INVALIDATED_TOUCH_INDEX


    var isAddPoint = false
        set(value) {
            field = value
            if (value) {
                initPoints(0f, measuredHeight.toFloat(), measuredWidth.toFloat(), 0f)
            }
            invalidate()
        }

    //控制点的画笔
    private val ctrlPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }


    //关键点的画笔
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    //路径的画笔
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    //辅助线的画笔
    private val helpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        initPoints(0f, h.toFloat(), w.toFloat(), 0f)
        invalidate()
    }


    private fun initPoints(startX: Float, startY: Float, endX: Float, endY: Float) {
        val startPoint = BezierPoint(startX, startY)
        val endPoint = BezierPoint(endX, endY)
        points.clear()
        points.add(startPoint)
        points.add(endPoint)
        buildPath()
    }


    //生成路径
    private fun buildPath() {
        if (points.size < 2) return
        path.reset()
        path.moveTo(points[0].x, points[0].y)
        (0 until points.size - 1).forEach {
            val p1 = points[it]
            val p2 = points[it + 1]
            Log.d("YUEDEVTAG", "index:$it")
            Log.d("YUEDEVTAG", "p1:$p1")
            Log.d("YUEDEVTAG", "p2:$p2")
            path.cubicTo(p1.ctrl2x, p1.ctrl2y, p2.ctrl1x, p2.ctrl1y, p2.x, p2.y)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (points.size < 2) return
        canvas.drawPath(path, pathPaint)
        (0 until points.size).forEach {
            val point = points[it]
            //画辅助线
            canvas.drawLine(point.ctrl1x, point.ctrl1y, point.ctrl2x, point.ctrl2y, helpPaint)
            //画控制点
            canvas.drawPoint(point.ctrl1x, point.ctrl1y, ctrlPointPaint)
            canvas.drawPoint(point.ctrl2x, point.ctrl2y, ctrlPointPaint)
            //画关键点
            canvas.drawPoint(point.x, point.y, pointPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        return when (event.action) {

            MotionEvent.ACTION_DOWN -> if (isAddPoint) {
                addPoint(event.x, event.y)
                invalidate()
                true
            } else
                calIndex(event.x, event.y)

            MotionEvent.ACTION_MOVE -> if (!isAddPoint) {
                val point = points[touchIndex]
                when (touchIndex) {
                    INVALIDATED_TOUCH_INDEX -> super.onTouchEvent(event)
                    0 -> {
                        point.ctrl2x = event.x
                        point.ctrl2y = event.y
                        buildPath()
                        invalidate()
                        true
                    }
                    points.lastIndex -> {
                        point.ctrl1x = event.x
                        point.ctrl1y = event.y
                        buildPath()
                        invalidate()
                        true
                    }
                    else -> {
                        point.ctrl1x = event.x
                        point.ctrl1y = event.y
                        val dx = point.x - point.ctrl1x
                        val dy = point.y - point.ctrl1y
                        point.ctrl2x = point.x + dx
                        point.ctrl2y = point.y + dy
                        buildPath()
                        invalidate()
                        true
                    }
                }
            } else
                super.onTouchEvent(event)

            MotionEvent.ACTION_CANCEL -> {
                touchIndex = INVALIDATED_TOUCH_INDEX
                super.onTouchEvent(event)
            }

            else -> super.onTouchEvent(event)
        }
    }


    //计算触摸的点index

    private fun calIndex(x: Float, y: Float): Boolean {
        (0 until points.size).forEach {
            val point = points[it]
            if (it == 0 && abs(x - point.ctrl2x) < TOUCH_AREA_SIZE && abs(y - point.ctrl2y) < TOUCH_AREA_SIZE) {
                touchIndex = it
                return true
            } else if ((abs(x - point.ctrl1x) < TOUCH_AREA_SIZE && abs(y - point.ctrl1y) < TOUCH_AREA_SIZE)) {
                touchIndex = it
                return true
            }
        }
        touchIndex = INVALIDATED_TOUCH_INDEX
        return false
    }


    private fun addPoint(x: Float, y: Float) {
        if (points.size < 2) return
        val point = BezierPoint(x, y)
        points.add(points.lastIndex, point)
        buildPath()
    }

    fun getInterpolator():PathInterpolator {

        //把控制点标准化到（0,0）(1,1)
        val w = measuredWidth
        val h = measuredHeight
        val newPoints = points.map {
            val x = it.x / w
            val y = 1 - it.y / h
            val ctrl1x = it.ctrl1x / w
            val ctrl1y = 1 - it.ctrl1y / h
            val ctrl2x = it.ctrl2x / w
            val ctrl2y = 1 - it.ctrl2y / h
            BezierPoint(x, y, ctrl1x, ctrl1y, ctrl2x, ctrl2y)
        }

        val newPath = Path()
        newPath.reset()
        newPath.moveTo(newPoints[0].x, newPoints[0].y)
        (0 until newPoints.size - 1).forEach {
            val p1 = newPoints[it]
            val p2 = newPoints[it + 1]
            newPath.cubicTo(p1.ctrl2x, p1.ctrl2y, p2.ctrl1x, p2.ctrl1y, p2.x, p2.y)
        }

        return PathInterpolator(newPath)
    }

}


data class BezierPoint(
    var x: Float,
    var y: Float,
    var ctrl1x: Float = x,
    var ctrl1y: Float = y,
    var ctrl2x: Float = x,
    var ctrl2y: Float = y
)