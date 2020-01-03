package com.cepiloth.ezledview.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.cepiloth.ezledview.EZLedView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekbarCircle.progress = ledView!!.ledRadius
        seekbarCircle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        seekbarSpace.progress = ledView!!.ledSpace
        seekbarSpace.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ledView!!.ledSpace = progress
                ledView!!.invalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekbarTextSize.progress = ledView!!.ledTextSize
        seekbarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                ledView!!.setDrawable(resources.getDrawable(R.drawable.choi))
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
            ll_led_type.visibility = View.VISIBLE
        } else {
            ll_led_type.visibility = View.INVISIBLE
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
