package photoeditor.photoart.onetap.beziertest

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import photoeditor.photoart.onetap.beziertest.view.KeyView

class KeyActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startNewInstance(context: Context) {
            context.startActivity(Intent(context, KeyActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key)

        val keyView = findViewById<KeyView>(R.id.keyView)

        val view = findViewById<View>(R.id.view)

        findViewById<View>(R.id.buttonAdd).setOnClickListener {
            keyView.isAddPoint = true
        }

        findViewById<View>(R.id.buttonClean).setOnClickListener {
            keyView.cleanPoints()
        }

        findViewById<View>(R.id.buttonPlay).setOnClickListener {
            view.translationX = 0f
            view.scaleX = 1f
            view.scaleY = 1f

            val end =  window.decorView.measuredWidth.toFloat() - view.measuredWidth * 1.5f + view.measuredWidth / 2f

            ViewCompat.animate(view).translationX(end).scaleX(1.5f).scaleY(1.5f).setDuration(2000).interpolator = keyView.getInterpolator()

        }


    }
}