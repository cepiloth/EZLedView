package com.goyourfly.ezledview.app

import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.HorizontalScrollView

import com.goyourfly.ezledview.EZLedView

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

        if(intent.hasExtra("type")) {
            var str = intent.getStringExtra("type")

            if(str == "image")
                ledView!!.setDrawable(resources.getDrawable(R.drawable.simpson))
        }

        handler.post(object : Runnable {
            override fun run() {
                scrollView.scrollTo(scrollX, 0)
                scrollX += (ledView.ledRadius + ledView.ledSpace) * direct

                var width = ledView.width;
                if(width < 3000) {
                    width = 3000;
                }
                if (scrollX <= 0 || scrollX >= width - scrollView.width) {
                    direct = -direct
                }
                handler.postDelayed(this, 10)
            }
        })

    }
}
