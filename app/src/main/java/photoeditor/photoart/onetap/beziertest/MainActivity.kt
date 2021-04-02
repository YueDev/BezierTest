package photoeditor.photoart.onetap.beziertest

import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.PathInterpolator
import android.widget.Button
import androidx.core.view.ViewCompat


class MainActivity : AppCompatActivity() {

    private val view by lazy { findViewById<View>(R.id.view) }
    private val bezierView by lazy { findViewById<BezierView>(R.id.imageView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_start).setOnClickListener {
            startAnimation()
        }


    }

    private fun startAnimation() {

        view.translationX = 0f

        //取出bezierView的控制点坐标，转换成(0, 0)到(1, 1)，给PathInterpolator用
        //注意纵坐标要用1减去，做翻转
        val c1x = bezierView.ctrl1.x / bezierView.measuredWidth
        val c1y = 1 - bezierView.ctrl1.y / bezierView.measuredHeight

        val c2x = bezierView.ctrl2.x / bezierView.measuredWidth
        val c2y = 1 - bezierView.ctrl2.y / bezierView.measuredHeight

        val path = Path()
        path.moveTo(0f, 0f)
        path.cubicTo(c1x, c1y, c2x, c2y, 1f, 1f)

        val end = window.decorView.measuredWidth - view.measuredWidth

        val pathInterpolator = PathInterpolator(path)
        ViewCompat.animate(view).translationX(end.toFloat()).setDuration(1000).interpolator = pathInterpolator


    }
}