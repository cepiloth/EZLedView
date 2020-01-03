package com.cepiloth.ezledview.app

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.HorizontalScrollView
import androidx.appcompat.app.AppCompatActivity
import com.cepiloth.ezledview.EZLedView


class LedDisplayActivity : AppCompatActivity() {
    internal var handler = Handler()
    internal var scrollX = 0
    internal var direct = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_led_display)

        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN

        window.decorView.systemUiVisibility = uiOptions

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        val scrollView = findViewById<View>(R.id.scrollView) as HorizontalScrollView
        val ledView = findViewById<View>(R.id.ledView) as EZLedView
        ledView!!.ledRadius = 3

        val hasExtra = intent.hasExtra("type")

        if(hasExtra) {
            var str = intent.getStringExtra("type")

            if(str == "image") {
                val sbitmap:Bitmap = BitmapFactory.decodeResource(resources, R.drawable.choi)

                val paint = Paint()
                paint.setShader(BitmapShader(sbitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT))

                val bitmap:Bitmap = Bitmap.createBitmap(sbitmap.width * 2, sbitmap.height, Bitmap.Config.RGB_565)
                val canvas = Canvas(bitmap)
                val rect = Rect(0,0,sbitmap.width * 2, sbitmap.height)
                canvas.drawRect(rect, paint)

                val drawable = BitmapDrawable(bitmap)
                ledView.setDrawable(drawable)
            }
        }

        handler.post(object : Runnable {
            override fun run() {
                scrollView.scrollTo(scrollX, 0)
                scrollX += (ledView.ledRadius + ledView.ledSpace) * direct

                if (scrollX <= 0 || scrollX >= ledView.width - scrollView.width) {
                    direct = -direct
                }
                handler.postDelayed(this, 10)
            }
        })
    }
}
