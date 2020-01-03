package com.cepiloth.ezledview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import java.util.*

/**
 * Created by gaoyufei on 2017/5/12.
 * 这是一个类似于模拟LED灯效果的自定义View
 * 可以实现将文本和图片转换成LED效果
 */

class EZLedView : View {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)


    /**
     * Led light space
     */
    var ledSpace: Int = 0

    /**
     * Led light radius
     */
    var ledRadius: Int = 0
    /**
     * Led color, if content is image,this param not work
     */
    private var ledColor: Int = 0

    /**
     * Content text size
     */
    var ledTextSize: Int = 0

    /**
     * Content type, text or image
     */
    var ledType: String? = null

    /**
     * Custom led light drawable
     */
    var ledLightDrawable: Drawable? = null

    /**
     * Store the points of all px
     */
    private val circlePoint = ArrayList<Point>()

    /**
     * Content of text
     */
    public var ledText: CharSequence? = null


    /**
     * Content of image
     */
    public var ledImage: Drawable? = null

    /**
     * The content width and height
     */
    private var mDrawableWidth: Int = 0
    private var mDrawableHeight: Int = 0

    private val mMaxWidth = Integer.MAX_VALUE
    private val mMaxHeight = Integer.MAX_VALUE
    private var sCompatAdjustViewBounds: Boolean = false

    /**
     * Padding have not added , so not working
     */
    private val mPaddingLeft = 0
    private val mPaddingRight = 0
    private val mPaddingTop = 0
    private val mPaddingBottom = 0

    private var contentType: String? = CONTENT_TYPE_TEXT


    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    /**
     * Read the xml config and init
     * @param attrs the xml config
     */
    private fun init(attrs: AttributeSet?) {
        val targetSdkVersion = context.applicationInfo.targetSdkVersion
        sCompatAdjustViewBounds = targetSdkVersion <= Build.VERSION_CODES.JELLY_BEAN_MR1
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK

        if (attrs == null)
            return
        val attributes = context.obtainStyledAttributes(attrs,
                R.styleable.EZLedView)
        ledRadius = attributes.getDimensionPixelSize(R.styleable.EZLedView_led_radius, 10)
        ledSpace = attributes.getDimensionPixelOffset(R.styleable.EZLedView_led_space, 2)
        ledTextSize = attributes.getDimensionPixelOffset(R.styleable.EZLedView_text_size, 100)

        ledColor = attributes.getColor(R.styleable.EZLedView_led_color, 0)
        ledType = attributes.getString(R.styleable.EZLedView_led_type)
        if (TextUtils.isEmpty(ledText))
            ledType = LED_TYPE_CIRCLE

        if (ledType == LED_TYPE_DRAWABLE) {
            val ledLightId = attributes.getResourceId(R.styleable.EZLedView_led_drawable, 0)
            if (ledLightId != 0) {
                ledLightDrawable = resources.getDrawable(ledLightId)
            }
            if (ledLightDrawable == null)
                throw RuntimeException("Drawable type need you set a image")
        }

        contentType = attributes.getString(R.styleable.EZLedView_content_type)
        if (TextUtils.isEmpty(contentType)) {
            contentType = CONTENT_TYPE_TEXT
        }

        val ledImageId = attributes.getResourceId(R.styleable.EZLedView_image, 0)
        if (ledImageId != 0) {
            ledImage = resources.getDrawable(ledImageId)
        }
        ledText = attributes.getText(R.styleable.EZLedView_text)

        paint.color = ledColor
        paint.textSize = ledTextSize.toFloat()

        if (!TextUtils.isEmpty(ledText)) {
            val ledTextEx: CharSequence = ledText as CharSequence
            setText(ledTextEx)
        } else if (ledImage != null) {
            val ledImageEx: Drawable = ledImage as Drawable
            setDrawable(ledImageEx)
        } else {
            throw NullPointerException("Neither ledImage nor ledText is available")
        }
    }

    /**
     * Resize the view if need
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Text type not need resize
        if (contentType == CONTENT_TYPE_TEXT) {
            setMeasuredDimension(mDrawableWidth, mDrawableHeight)
            return
        }
        var w: Int
        var h: Int

        val pleft = mPaddingLeft
        val pright = mPaddingRight
        val ptop = mPaddingTop
        val pbottom = mPaddingBottom
        // Desired aspect ratio of the view's contents (not including padding)
        var desiredAspect = 0.0f

        // We are allowed to change the view's mWidth
        var resizeWidth = false

        // We are allowed to change the view's mHeight
        var resizeHeight = false


        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)


        w = mDrawableWidth
        h = mDrawableHeight
        if (w <= 0) w = 1
        if (h <= 0) h = 1

        // We are supposed to adjust view bounds to match the aspect
        // ratio of our drawable. See if that is possible.
        resizeWidth = widthSpecMode != View.MeasureSpec.EXACTLY
        resizeHeight = heightSpecMode != View.MeasureSpec.EXACTLY

        desiredAspect = w.toFloat() / h.toFloat()

        var widthSize: Int
        var heightSize: Int


        if (resizeWidth || resizeHeight) {

            // Get the max possible mWidth given our constraints
            widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec)

            // Get the max possible mHeight given our constraints
            heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec)

            // See what our actual aspect ratio is
            val actualAspect = (widthSize - pleft - pright).toFloat() / (heightSize - ptop - pbottom)
            if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                var done = false

                // Try adjusting mWidth to be proportional to mHeight
                if (resizeWidth) {
                    val newWidth = (desiredAspect * (heightSize - ptop - pbottom)).toInt() +
                            pleft + pright

                    // Allow the mWidth to outgrow its original estimate if mHeight is fixed.
                    if (!resizeHeight && !sCompatAdjustViewBounds) {
                        widthSize = resolveAdjustedSize(newWidth, mMaxWidth, widthMeasureSpec)
                    }

                    if (newWidth <= widthSize) {
                        widthSize = newWidth
                        done = true
                    }
                }

                // Try adjusting mHeight to be proportional to mWidth
                if (!done && resizeHeight) {
                    val newHeight = ((widthSize - pleft - pright) / desiredAspect).toInt() +
                            ptop + pbottom

                    // Allow the mHeight to outgrow its original estimate if mWidth is fixed.
                    if (!resizeWidth && !sCompatAdjustViewBounds) {
                        heightSize = resolveAdjustedSize(newHeight, mMaxHeight,
                                heightMeasureSpec)
                    }

                    if (newHeight <= heightSize) {
                        heightSize = newHeight
                    }
                }
            }
        } else {
            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright
            h += ptop + pbottom

            w = Math.max(w, suggestedMinimumWidth)
            h = Math.max(h, suggestedMinimumHeight)

            widthSize = View.resolveSizeAndState(w, widthMeasureSpec, 0)
            heightSize = View.resolveSizeAndState(h, heightMeasureSpec, 0)
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    private fun resolveAdjustedSize(desiredSize: Int, maxSize: Int,
                                    measureSpec: Int): Int {
        var result = desiredSize
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)
        when (specMode) {
            View.MeasureSpec.UNSPECIFIED ->
                /* Parent says we can be as big as we want. Just don't be larger
                   than max size imposed on ourselves.
                */
                result = Math.min(desiredSize, maxSize)
            View.MeasureSpec.AT_MOST ->
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(Math.min(desiredSize, specSize), maxSize)
            View.MeasureSpec.EXACTLY ->
                // No choice. Do what we are told.
                result = specSize
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        var bitmap: Bitmap? = null
        if (contentType == CONTENT_TYPE_TEXT) {
            bitmap = generateDrawable(renderText(ledText!!, paint))
        } else if (contentType == CONTENT_TYPE_IMAGE) {
            bitmap = generateDrawable(renderDrawable(ledImage, width, height))
        }

        if (bitmap != null) {
            if (bitmap.width > TEXTURE_MAX) {
                val count = Math.ceil((bitmap.width / TEXTURE_MAX.toFloat()).toDouble()).toInt()
                for (i in 0 until count) {
                    val x = i * TEXTURE_MAX
                    var width = TEXTURE_MAX
                    if (x + width > bitmap.width) {
                        width = bitmap.width - x
                    }
                    val newBitmap = Bitmap.createBitmap(bitmap, x, 0, width, bitmap.height)
                    canvas.drawBitmap(newBitmap, x.toFloat(), 0f, paint)
                }
            } else {
                canvas.drawBitmap(bitmap, 0f, 0f, paint)
            }
        }
    }


    @Deprecated("")
    private fun setLEDView(view: View) {

    }

    /**
     * Set text content
     * @param text content
     */
    fun setText(text: CharSequence) {
        this.contentType = CONTENT_TYPE_TEXT
        this.ledText = text
        measureTextBound(text.toString())
        requestLayout()
        invalidate()
    }


    /**
     * Set drawable content
     * @param drawable drawable
     */
    fun setDrawable(drawable: Drawable) {
        this.contentType = CONTENT_TYPE_IMAGE
        this.ledImage = drawable
        mDrawableWidth = drawable.intrinsicWidth
        mDrawableHeight = drawable.intrinsicHeight
        requestLayout()
        invalidate()
    }

    private fun generateDrawable(bitmap: Bitmap?): Bitmap? {
        if (bitmap != null) {
            release()
            measureBitmap(bitmap)
            return generateLedBitmap(bitmap)
        }
        return null
    }

    /**
     * measure the text width and height
     * @param text text content
     */
    private fun measureTextBound(text: String) {
        val m = paint.fontMetrics
        mDrawableWidth = paint.measureText(text).toInt()
        mDrawableHeight = (m.bottom - m.ascent).toInt()
    }

    /**
     * Transform text to bitmap
     * @param text text content
     * @param paint paint
     * @return the bitmap of text
     */
    private fun renderText(text: CharSequence, paint: Paint): Bitmap {
        //        DynamicLayout dynamicLayout = new DynamicLayout(
        //                text,
        //                new TextPaint(paint),
        //                width,
        //                Layout.Alignment.ALIGN_CENTER,
        //                0,
        //                0,
        //                false
        //        );
        val bitmap = Bitmap.createBitmap(mDrawableWidth, mDrawableHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        //        dynamicLayout.draw(canvas);
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
        canvas.drawText(text.toString(), 0f, yPos.toFloat(), paint)
        return bitmap
    }


    /**
     * Transform a bitmap to a led bitmap
     * @param src the original bitmap
     * @return led bitmap
     */
    private fun generateLedBitmap(src: Bitmap): Bitmap {
        val bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        for (point in circlePoint) {
            // Detect if the px is in range of our led position
            var color = isInRange(src, point.x, point.y)
            if (color != 0) {
                if (ledColor != 0 && contentType != CONTENT_TYPE_IMAGE) {
                    color = ledColor
                }
                paint.color = color

                // draw shape according to ledType
                if (LED_TYPE_CIRCLE == ledType) {
                    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), ledRadius.toFloat(), paint)
                } else if (LED_TYPE_SQUARE == ledType) {
                    canvas.drawRect((point.x - ledRadius).toFloat(), (point.y - ledRadius).toFloat(), (point.x + ledRadius).toFloat(), (point.y + ledRadius).toFloat(), paint)
                } else if (LED_TYPE_DRAWABLE == ledType) {
                    ledLightDrawable!!.setBounds(point.x - ledRadius, point.y - ledRadius, point.x + ledRadius, point.y + ledRadius)
                    ledLightDrawable!!.draw(canvas)
                }
            }
        }
        return bitmap
    }

    fun release() {
        circlePoint.clear()
    }


    private fun measureBitmap(bitmap: Bitmap) {
        measurePoint(bitmap.width, bitmap.height)
    }


    /**
     * Calculate the led point position
     */
    private fun measurePoint(width: Int, height: Int) {
        val halfBound = ledRadius + ledSpace / 2
        var x = halfBound
        var y = halfBound
        while (true) {
            while (true) {
                circlePoint.add(Point(x, y))
                y += halfBound * 2
                if (y > height) {
                    y = halfBound
                    break
                }
            }
            x += halfBound * 2
            if (x > width) {
                break
            }
        }
    }

    private fun isInCircleLeft(bitmap: Bitmap, x: Int, y: Int): Int {
        if (x - ledRadius > 0 && x - ledRadius < bitmap.width
                && y > 0 && y < bitmap.height) {
            val pxL = bitmap.getPixel(x - ledRadius, y)
            if (pxL != 0)
                return pxL
        }
        return 0
    }

    private fun isInCircleTop(bitmap: Bitmap, x: Int, y: Int): Int {
        if (y - ledRadius > 0 && y - ledRadius < bitmap.height
                && x > 0 && x < bitmap.width) {
            val pxT = bitmap.getPixel(x, y - ledRadius)
            if (pxT != 0)
                return pxT
        }
        return 0
    }

    private fun isInCircleRight(bitmap: Bitmap, x: Int, y: Int): Int {
        if (x + ledRadius > 0 && x + ledRadius < bitmap.width
                && y > 0 && y < bitmap.height) {
            val pxR = bitmap.getPixel(x + ledRadius, y)
            if (pxR != 0)
                return pxR
        }
        return 0
    }


    private fun isInCircleBottom(bitmap: Bitmap, x: Int, y: Int): Int {
        if (y + ledRadius > 0 && y + ledRadius < bitmap.height
                && x > 0 && x < bitmap.width) {
            val pxB = bitmap.getPixel(x, y + ledRadius)
            if (pxB != 0)
                return pxB
        }
        return 0
    }

    private fun isInCircleCenter(bitmap: Bitmap, x: Int, y: Int): Int {
        if (y > 0 && y < bitmap.height
                && x > 0 && x < bitmap.width) {
            val pxC = bitmap.getPixel(x, y)
            if (pxC != 0)
                return pxC
        }
        return 0
    }

    /**
     * Measure if x and y is in range of leds
     * @param bitmap the origin bitmap
     * @param x led x
     * @param y led y
     * @return the color , if color is zero means empty
     */
    private fun isInRange(bitmap: Bitmap?, x: Int, y: Int): Int {
        if (bitmap == null)
            return 0
        val pxL = isInCircleLeft(bitmap, x, y)
        val pxT = isInCircleTop(bitmap, x, y)
        val pxR = isInCircleRight(bitmap, x, y)
        val pxB = isInCircleBottom(bitmap, x, y)
        val pxC = isInCircleCenter(bitmap, x, y)

        var num = 0
        if (pxL != 0) {
            num++
        }
        if (pxT != 0) {
            num++
        }
        if (pxR != 0) {
            num++
        }
        if (pxB != 0) {
            num++
        }
        if (pxC != 0) {
            num++
        }
        if (num >= 2) {
            val a = Color.alpha(pxL) + Color.alpha(pxT) + Color.alpha(pxR) + Color.alpha(pxB) + Color.alpha(pxC)
            val r = Color.red(pxL) + Color.red(pxT) + Color.red(pxR) + Color.red(pxB) + Color.red(pxC)
            val g = Color.green(pxL) + Color.green(pxT) + Color.green(pxR) + Color.green(pxB) + Color.green(pxC)
            val b = Color.blue(pxL) + Color.blue(pxT) + Color.blue(pxR) + Color.blue(pxB) + Color.blue(pxC)
            return Color.argb(a / 5, r / 5, g / 5, b / 5)
        }

        return 0
    }

    fun getLedColor(): Int {
        return ledColor
    }

    fun setLedColor(ledColor: Int) {
        this.ledColor = ledColor
        paint.color = ledColor
    }

    @JvmName("getLedTextSizeInt")
    fun getLedTextSize(): Int {
        return ledTextSize
    }

    @JvmName("setLedTextSizeInt")
    fun setLedTextSize(ledTextSize: Int) {
        if (ledText == null)
            throw NullPointerException("Please set ledText before setLedTextSize")
        this.ledTextSize = ledTextSize
        measureTextBound(ledText!!.toString())
        paint.textSize = ledTextSize.toFloat()
    }

    companion object {
        private val TAG = "EZLedLayout"
        /**
         * The max size of a bitmap
         */
        private val TEXTURE_MAX = 2 * 1024
        /**
         * Led light show shape
         * 1.circle shape
         * 2.square shape
         * 3.custom shape
         */
        val LED_TYPE_CIRCLE = "1"
        val LED_TYPE_SQUARE = "2"
        val LED_TYPE_DRAWABLE = "3"

        /**
         * Content type,text or image
         */
        val CONTENT_TYPE_TEXT = "1"
        val CONTENT_TYPE_IMAGE = "2"

        /**
         * Transform the image drawable to bitmap
         * @param drawable the content drawable
         * @param width the new bitmap width
         * @param height the new bitmap height
         * @return bitmap of drawable
         */
        private fun renderDrawable(drawable: Drawable?, width: Int, height: Int): Bitmap {
            val bitmap = getBitmapFromDrawable(drawable)
            return Bitmap.createScaledBitmap(bitmap!!, width, height, true)
        }

        @Deprecated("")
        private fun loadBitmapFromView(v: View): Bitmap {
            if (v.measuredHeight <= 0 || v.layoutParams == null) {
                v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                v.layout(0, 0, v.measuredWidth, v.measuredHeight)
                v.draw(c)
                return b
            }
            val b = Bitmap.createBitmap(v.layoutParams.width, v.layoutParams.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            v.layout(v.left, v.top, v.right, v.bottom)
            v.draw(c)
            return b
        }

        /**
         * Get bitmap from drawable, Copy from CircleImageView
         * @param drawable the drawable
         * @return the bitmap of drawable
         */
        private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
            if (drawable == null) {
                return null
            }

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            try {
                val bitmap: Bitmap

                if (drawable is ColorDrawable) {
                    bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
                } else {
                    bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ALPHA_8)
                }

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }
    }
}
