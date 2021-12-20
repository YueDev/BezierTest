package photoeditor.photoart.onetap.beziertest.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.util.lruCache
import kotlin.math.abs

/**
 * Created by Yue on 2021/12/17.
 */

class KeyView : View {

    companion object {

        const val INVALIDATED_TOUCH_INDEX = -1

        //触摸区域
        const val TOUCH_AREA_SIZE = 48f

        //两个点之间的间隔，即一个点的右控制点与另一个点的左控制点的间隔.
        const val POINT_PADDING = 16f

        //控制点和关键点的间隔
        private var CTRL_PADDING = 96f

    }

    //点
    private val points = mutableListOf<KeyPoint>()

    //路径
    private val path = Path()

    //移动点的index
    private var touchIndex = INVALIDATED_TOUCH_INDEX

    //是否是添加点的模式
    var isAddPoint = false



    //控制点的画笔
    private val ctrlPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    //辅助线的画笔
    private val helpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }


    //关键点的画笔
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    //路径的画笔
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 6f
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
        val startPoint = KeyPoint(startX, startY, CTRL_PADDING, CTRL_PADDING)
        val endPoint = KeyPoint(endX, endY, CTRL_PADDING, CTRL_PADDING)
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
            path.cubicTo(p1.getRightX(), p1.getRightY(), p2.getLeftX(), p2.getLeftY(), p2.x, p2.y)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (points.size < 2) return
        canvas.drawPath(path, pathPaint)
        (0 until points.size).forEach {
            val point = points[it]
            canvas.drawCircle(point.x, point.y, 18f, pointPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        return when (event.action) {

            MotionEvent.ACTION_DOWN -> if (isAddPoint) {
                //加点状态下 添加成功
                addPoint(event.x, event.y)
                invalidate()
                true
            } else
                //非加点模式，即移动模式
                calIndex(event.x, event.y)

            MotionEvent.ACTION_MOVE -> if (!isAddPoint) {
                when (touchIndex) {
                    INVALIDATED_TOUCH_INDEX -> super.onTouchEvent(event)
                    else -> {
                        val point = points[touchIndex]
                        val prevPoint = points[touchIndex - 1]
                        val nextPoint = points[touchIndex + 1]


                        val leftLimit = prevPoint.x + POINT_PADDING
                        val rightLimit = nextPoint.x - POINT_PADDING
                        point.x = event.x.coerceIn(leftLimit, rightLimit)
                        point.y = event.y
                        //根据三个点的距离计算padding
                        val leftPadding = CTRL_PADDING.coerceAtMost((point.x - prevPoint.x - POINT_PADDING) / 2f)
                        val rightPadding = CTRL_PADDING.coerceAtMost((nextPoint.x - point.x - POINT_PADDING) / 2f)

                        point.leftPadding = leftPadding
                        prevPoint.rightPadding = leftPadding
                        point.rightPadding = rightPadding
                        nextPoint.leftPadding = rightPadding
                        buildPath()
                        invalidate()
                        true
                    }
                }
            } else
                super.onTouchEvent(event)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchIndex = INVALIDATED_TOUCH_INDEX
                isAddPoint = false
                super.onTouchEvent(event)
            }

            else -> super.onTouchEvent(event)
        }
    }


    //计算触摸的点index，首尾不能移动， 不判断
    private fun calIndex(x: Float, y: Float): Boolean {
        (1 until points.lastIndex).forEach {
            val point = points[it]
            if (abs(x - point.x) < TOUCH_AREA_SIZE && abs(y - point.y) < TOUCH_AREA_SIZE) {
                touchIndex = it
                return true
            }
        }
        touchIndex = INVALIDATED_TOUCH_INDEX
        return false
    }



    //添加点，遍历1 到最后一个点，判断添加的index
    private fun addPoint(x: Float, y: Float): Boolean {
        if (points.size < 2) return false
        (1..points.lastIndex).forEach {
            val prevPoint = points[it - 1]
            val nextPoint = points[it]

            val leftLimit = prevPoint.x + POINT_PADDING
            val rightLimit = nextPoint.x - POINT_PADDING

            if (x in (leftLimit..rightLimit)) {
                val newPoint = KeyPoint(x, y, 0f, 0f)

                //根据三个点的距离计算padding
                val leftPadding = CTRL_PADDING.coerceAtMost((newPoint.x - prevPoint.x - POINT_PADDING) / 2f)
                val rightPadding = CTRL_PADDING.coerceAtMost((nextPoint.x - newPoint.x - POINT_PADDING) / 2f)

                newPoint.leftPadding = leftPadding
                prevPoint.rightPadding = leftPadding
                newPoint.rightPadding = rightPadding
                nextPoint.leftPadding = rightPadding

                points.add(it, newPoint)
                buildPath()
                return true
            }
        }
        return false
    }

    fun cleanPoints() {
        initPoints(0f, measuredHeight.toFloat(), measuredWidth.toFloat(), 0f)
        invalidate()
    }


    fun getInterpolator(): PathInterpolator {

        //把控制点标准化到（0,0）(1,1)
        val w = measuredWidth
        val h = measuredHeight
        val newPoints = points.map {
            val x = it.x / w
            val y = 1 - it.y / h
            val leftPadding = it.leftPadding / w
            val rightPadding = it.rightPadding / w

            val point = KeyPoint(x, y, leftPadding, rightPadding)
            point
        }

        val newPath = Path()
        newPath.reset()
        newPath.moveTo(newPoints[0].x, newPoints[0].y)
        (0 until newPoints.size - 1).forEach {
            val p1 = newPoints[it]
            val p2 = newPoints[it + 1]
            newPath.cubicTo(p1.getRightX(), p1.getRightY(), p2.getLeftX(), p2.getLeftY(), p2.x, p2.y)
        }

        return PathInterpolator(newPath)
    }
}


//关键点，padding是左右两个控制点的距离
data class KeyPoint(
    var x: Float,
    var y: Float,
    var leftPadding: Float,
    var rightPadding: Float
) {
    fun getLeftX() = x - leftPadding
    fun getLeftY() = y
    fun getRightX() = x + rightPadding
    fun getRightY() = y
}

