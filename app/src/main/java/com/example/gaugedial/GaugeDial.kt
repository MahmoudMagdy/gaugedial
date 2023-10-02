package com.example.gaugedial

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.gaugedial.model.CircularBackground
import com.example.gaugedial.model.FillPaint
import com.example.gaugedial.model.Frame
import com.example.gaugedial.model.Hint
import com.example.gaugedial.model.Indication
import com.example.gaugedial.model.InnerCentralCircle
import com.example.gaugedial.model.InnerRim
import com.example.gaugedial.model.LargeSerif
import com.example.gaugedial.model.Needle
import com.example.gaugedial.model.OuterCentralCircle
import com.example.gaugedial.model.OuterRim
import com.example.gaugedial.model.Point
import com.example.gaugedial.model.SmallSerif
import com.example.gaugedial.model.Text
import com.example.gaugedial.util.DisplaySizeConverter
import kotlin.math.abs

class GaugeDial(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Runnable {
    companion object {
        private const val FRAMES_PER_SECOND = 60.0
        private const val BACKGROUND_COLOR = Color.GRAY
        private const val LARGE_SERIFS_START_ANGLE = 150f
        private const val LARGE_SERIFS_MAX_ANGLE = 240f
        private const val LARGE_SERIF_MAX_VALUE_DEFAULT = 240
        private const val LARGE_SERIF_STEP_DEFAULT = 20
        private const val SMALL_SERIFS_INTERMEDIATE_COUNT_DEFAULT = 1
    }

    private var canDraw = false
    private var thread: Thread? = null
    private val frame = Frame(fps = FRAMES_PER_SECOND)
    private var canvas: Canvas? = null

    private val backgroundBrush = FillPaint(BACKGROUND_COLOR)

    private var theta: Double = toRadian(LARGE_SERIFS_START_ANGLE)
    private var thetaPerSec: Double = 0.0
    private var desiredLastFrameTheta: Double = toRadian(LARGE_SERIFS_START_ANGLE)
    private var previousDesiredLastFrameTheta: Double = toRadian(LARGE_SERIFS_START_ANGLE)

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels
    private val centralPoint = Point(screenWidth / 2f, screenHeight / 2f)
    private val margin = toPxs(dps = 12)
    private val displaySize: Float = getDisplaySize()
    private val converter = DisplaySizeConverter(displaySize)

    private val circularBackground = CircularBackground(centralPoint, displaySize)
    private val outerCentralCircle = OuterCentralCircle(centralPoint, converter)
    private val innerCentralCircle = InnerCentralCircle(centralPoint, converter)
    private val outerRim = OuterRim(centralPoint, displaySize, converter)
    private val innerRim = InnerRim(centralPoint, displaySize, converter)
    private val largeSerif = LargeSerif(converter)
    private val smallSerif = SmallSerif(converter)
    private val indication = Indication(converter)
    private val hint = Hint(converter)
    private val needle = Needle(converter)

    private var hintText = ""

    private var largeSerifMaxValue = 240
        private set(value) {
            field = value
            angleRation = calculateAngleRatio()
            largeSerifRange = createLargeSerifRange()
        }

    private var largeSerifStep = 20
        private set(value) {
            field = value
            largeSerifRange = createLargeSerifRange()
        }

    private var smallSerifCount = 1

    private var angleRation = calculateAngleRatio()
    private var largeSerifRange: IntProgression = createLargeSerifRange()

    private var isScrolling = false
    private var startX = 0f
    private var startY = 0f
    private var contentOffsetX = 0f
    private var contentOffsetY = 0f

    private var onTwoFingerScrollListener: OnTwoFingerScrollListener? = null

    private fun createLargeSerifRange() = 0..largeSerifMaxValue step largeSerifStep

    private fun calculateAngleRatio() = LARGE_SERIFS_MAX_ANGLE / largeSerifMaxValue

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GaugeDial,
            0, 0
        ).apply {

            try {
                largeSerifMaxValue = getInteger(
                    R.styleable.GaugeDial_largeSerifMaxValue,
                    LARGE_SERIF_MAX_VALUE_DEFAULT
                )
                largeSerifStep =
                    getInteger(R.styleable.GaugeDial_largeSerifStep, LARGE_SERIF_STEP_DEFAULT)

                smallSerifCount = getInteger(
                    R.styleable.GaugeDial_smallSerifCount,
                    SMALL_SERIFS_INTERMEDIATE_COUNT_DEFAULT
                )

                hintText = getString(R.styleable.GaugeDial_hintText) ?: ""

            } finally {
                recycle()
            }
        }
    }

    private fun getDisplaySize(): Float {
        return (if (screenWidth < screenHeight) screenWidth else screenHeight) - margin
    }

    override fun run() {
        frame.start()
        var count = 0
        while (canDraw/* && count < 4 * FRAMES_PER_SECOND*/) {
            if (count != 0) count++
            if (!holder.surface.isValid || frame.drop()) {
                continue
            }
            if (count == 0) count = 1
            update(frame)
            draw()
            frame.end()
            try {
                if (frame.timeDelta > 0) {
                    Thread.sleep((frame.timeDelta / 1000_000).toLong())
                } else {
                    Thread.sleep(frame.overTime / 1000_000)
                }
            } catch (exception: InterruptedException) {

            }
            frame.next()
        }
    }

    private fun update(frame: Frame) {
        var inc = frame.time.nanoseconds
        if (frame.timeDelta < 0) {
            inc -= frame.timeDelta
        }
        theta += thetaPerSec * inc
        val testTheta = this.theta.let { if (it <= Math.PI / 6) it + 2 * Math.PI else it }
        if (testTheta > desiredLastFrameTheta && testTheta > previousDesiredLastFrameTheta) {
            theta = desiredLastFrameTheta
        }
        if (testTheta < desiredLastFrameTheta && testTheta < previousDesiredLastFrameTheta) {
            theta = desiredLastFrameTheta
        }
        if (theta > (2 * Math.PI)) {
            theta -= (2 * Math.PI)
        }
        if (frame.isLast) {
            theta = desiredLastFrameTheta
        }
    }

    private fun draw() {
        canvas = holder.lockCanvas()
        canvas?.translate(0f, -contentOffsetY)
        // draw background
        canvas?.drawColor(BACKGROUND_COLOR)
        // draw circular background
        circularBackground.draw(canvas)

        needle.drawStrategy.update(
            needle.segmentOnCircle(
                theta.toFloat(),
                circularBackground
            ).copy(end = centralPoint)
        )
        needle.draw(canvas)
        // draw hint
        hint.drawStrategy.update(
            Text.Info(
                hint.pointOnCircle(
                    toRadian(270f).toFloat(),
                    circularBackground
                ),
                270f,
                hintText
            )
        )
        hint.draw(canvas)
        // draw outer central circle
        outerCentralCircle.draw(canvas)
        // draw inner central circle
        innerCentralCircle.draw(canvas)
        // draw inner rim
        innerRim.draw(canvas)
        // draw outer rim
        outerRim.draw(canvas)

        drawSerifs()
        holder.unlockCanvasAndPost(canvas)
    }

    private fun drawSerifs() {
        val smallSerifStep = (largeSerifStep.toFloat() / (smallSerifCount + 1)) * angleRation
        var drawingAngle: Float
        for (angle in largeSerifRange) {
            drawingAngle = LARGE_SERIFS_START_ANGLE + (angle * angleRation)
            if (drawingAngle > 360) {
                drawingAngle -= 360
            }
            largeSerif.drawStrategy.update(
                largeSerif.segmentOnCircle(
                    toRadian(drawingAngle).toFloat(),
                    circularBackground
                )
            )
            largeSerif.draw(canvas)

            indication.drawStrategy.update(
                Text.Info(
                    indication.pointOnCircle(
                        toRadian(drawingAngle).toFloat(),
                        circularBackground
                    ),
                    drawingAngle,
                    angle.toString()
                )
            )
            indication.draw(canvas)
            if (smallSerifStep < largeSerifStep * angleRation && angle != largeSerifRange.last) {
                for (serifIndex in 0 until smallSerifCount) {
                    smallSerif.drawStrategy.update(
                        smallSerif.segmentOnCircle(
                            toRadian(drawingAngle + (serifIndex + 1) * smallSerifStep).toFloat(),
                            circularBackground
                        )
                    )
                    smallSerif.draw(canvas)
                }
            }
        }
    }

    private fun toPxs(dps: Int) = dps * resources.displayMetrics.density
    private fun toRadian(degree: Float) = (degree * Math.PI) / 180
    private fun toDegree(radian: Double) = (radian * 180) / Math.PI
    fun updateTheta(theta: Float) {
        val theta = theta * angleRation
        previousDesiredLastFrameTheta = desiredLastFrameTheta
        desiredLastFrameTheta = toRadian(LARGE_SERIFS_START_ANGLE + theta)
        this.thetaPerSec =
            (desiredLastFrameTheta - this.theta.let { if (it <= Math.PI / 6) it + 2 * Math.PI else it }) / 1000_000_000
    }

    fun resume() {
        canDraw = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause() {
        canDraw = false
        while (true) {
            try {
                thread?.join()
                break
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }
        }
        thread = null
    }

    fun setOnTwoFingerScrollListener(listener: OnTwoFingerScrollListener) {
        this.onTwoFingerScrollListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.getX(0)
                startY = event.getY(0)
                isScrolling = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isScrolling && event.pointerCount == 2) {
                    val deltaX = event.getX(0) - startX
                    val deltaY = event.getY(0) - startY

                    // Adjust content offset based on two-finger movement
                    contentOffsetX -= deltaX
                    contentOffsetY -= deltaY

                    if (abs(contentOffsetY) > screenHeight / 2 && canDraw) {
                        onTwoFingerScrollListener?.onTwoFingerScroll(deltaY)
                    }
                    startX = event.getX(0)
                    startY = event.getY(0)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                contentOffsetX = 0f
                contentOffsetY = 0f
                isScrolling = false
            }
        }
        return true
    }

    interface OnTwoFingerScrollListener {
        fun onTwoFingerScroll(distance: Float)
    }
}