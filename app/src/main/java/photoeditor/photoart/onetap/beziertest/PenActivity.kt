package photoeditor.photoart.onetap.beziertest

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import photoeditor.photoart.onetap.beziertest.view.PenView

class PenActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startNewInstance(context: Context) {
            context.startActivity(Intent(context, PenActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pen)

        val penView = findViewById<PenView>(R.id.penView)
        val addView = findViewById<View>(R.id.buttonAdd)
        val stopAddView = findViewById<View>(R.id.buttonStopAdd)
        val startView = findViewById<View>(R.id.buttonStart)
        val animateView = findViewById<View>(R.id.view)

        addView.setOnClickListener {
            penView.isAddPoint = true
        }

        stopAddView.setOnClickListener {
            penView.isAddPoint = false
        }

        startView.setOnClickListener {
            try {
                animateView.translationX = 0f
                val end = window.decorView.measuredWidth - animateView.measuredWidth
                val interpolator = penView.getInterpolator()
                ViewCompat.animate(animateView).translationX(end.toFloat()).setDuration(2000).interpolator = interpolator
            } catch (e: Exception) {
            }
        }

    }


}