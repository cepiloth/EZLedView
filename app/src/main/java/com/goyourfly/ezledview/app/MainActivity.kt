package com.goyourfly.ezledview.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import com.goyourfly.ezledview.EZLedView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var ledView: EZLedView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ledView = findViewById<View>(R.id.ledView) as EZLedView

        val circleRadius = findViewById<View>(R.id.seekbarCircle) as SeekBar
        circleRadius.progress = ledView!!.ledRadius
        circleRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress < 2)
                    return
                ledView!!.ledRadius = progress
                ledView!!.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val ledSpace = findViewById<View>(R.id.seekbarSpace) as SeekBar
        ledSpace.progress = ledView!!.ledSpace
        ledSpace.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ledView!!.ledSpace = progress
                ledView!!.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val textSize = findViewById<View>(R.id.seekbarTextSize) as SeekBar
        textSize.progress = ledView!!.ledTextSize
        textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ledView!!.ledTextSize = progress
                ledView!!.requestLayout()
                ledView!!.invalidate()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })



        //
        setRadioCheckListener(R.id.led_type, RadioGroup.OnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_text) {
                onTextEdit(true)
                ledView!!.setText("write your message")
            } else {
                onTextEdit(false)
                ledView!!.setDrawable(resources.getDrawable(R.drawable.simpson))
            }
        })


        //
        setRadioCheckListener(R.id.point_type, RadioGroup.OnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rb_circle) {
                ledView!!.ledType = EZLedView.LED_TYPE_CIRCLE
            } else if (checkedId == R.id.rb_square) {
                ledView!!.ledType = EZLedView.LED_TYPE_SQUARE
            } else {
                ledView!!.ledLightDrawable = resources.getDrawable(R.drawable.ic_star_black_24dp)
                ledView!!.ledType = EZLedView.LED_TYPE_DRAWABLE
            }
            ledView!!.invalidate()
        })

        onTextEdit(true)
    }

    /*
     * @brief : on off
     */
    private fun onTextEdit(on: Boolean) {

        if(on) {
            et_text_view.visibility = View.VISIBLE
            et_text.visibility = View.VISIBLE
        } else {
            et_text_view.visibility = View.INVISIBLE
            et_text.visibility = View.INVISIBLE
        }
    }

    private fun setRadioCheckListener(id: Int, listener: RadioGroup.OnCheckedChangeListener) {
        val radioGroup = findViewById<View>(id) as RadioGroup
        radioGroup.setOnCheckedChangeListener(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_led) {
            val intent = Intent(this, LedDisplayActivity::class.java);

            if(rb_image.isChecked) {
                intent.putExtra("type", "image")
            } else {
                intent.putExtra("type", "text")
                intent.putExtra("content", et_text.text)
            }

            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
