package com.example.gaugedial.model

import android.graphics.Canvas
import android.graphics.Color
import com.example.gaugedial.controller.strategy.CircleDrawStrategy
import com.example.gaugedial.controller.strategy.DrawStrategy
import com.example.gaugedial.controller.strategy.LineDrawStrategy
import com.example.gaugedial.controller.strategy.TextDrawStrategy
import com.example.gaugedial.util.DisplaySizeConverter
import kotlin.math.cos
import kotlin.math.sin

sealed class Shape(
    protected val margin: Float,
    color: Int,
    @BrushType brushType: Int,
    strokeWidth: Float = 0f
) {
    abstract val drawStrategy: DrawStrategy
    val brush = Brush(brushType, color, strokeWidth)
    fun draw(canvas: Canvas?) {
        drawStrategy.draw(canvas, this)
    }
}

sealed class Circle(
    val central: Point,
    diameter: Float,
    color: Int,
    @BrushType brushType: Int,
    width: Float = 0f,
    margin: Float = width
) : Shape(margin, color, brushType, width) {
    val radius: Float = (diameter - margin) / 2
    override val drawStrategy: CircleDrawStrategy = CircleDrawStrategy()

    fun point(angle: Float) =
        Point(central.x + radius * cos(angle), central.y + radius * sin(angle))

    fun point(angle: Float, distance: Float) =
        Point(
            central.x + (radius - distance) * cos(angle),
            central.y + (radius - distance) * sin(angle)
        )
}

sealed class Line(
    private val width: Float,
    private val height: Float,
    margin: Float,
    color: Int,
    @BrushType brushType: Int
) : Shape(margin, color, brushType, width) {

    override val drawStrategy: LineDrawStrategy = LineDrawStrategy()

    fun segmentOnCircle(angle: Float, circle: Circle) =
        Segment(circle.point(angle, margin), circle.point(angle, height + margin))

    data class Segment(val start: Point, val end: Point)
}

sealed class Text(
    val size: Float,
    margin: Float,
    color: Int,
    @BrushType brushType: Int
) : Shape(margin, color, brushType, 0f) {

    override val drawStrategy: TextDrawStrategy = TextDrawStrategy()

    fun pointOnCircle(angle: Float, circle: Circle) =
        circle.point(angle, margin).let { it.copy(y = it.y + size / 2) }

    fun segmentOnCircle(angle: Float, circle: Circle) =
        Line.Segment(circle.point(angle, margin), circle.point(angle, size + margin))

    data class Info(val point: Point, val angle: Float, val value: String)
}

// Solid Implementation/Reference for certain samples
// Circles
class CircularBackground(central: Point, displaySize: Float) :
    Circle(
        central,
        displaySize,
        Color.parseColor("#16182D"),
        FillPaint.TYPE,
    )

class OuterCentralCircle(central: Point, converter: DisplaySizeConverter) :
    Circle(
        central,
        converter.convertSampleToDisplay(88f),
        Color.parseColor("#E0EDE6"),
        FillPaint.TYPE,
    )

class InnerCentralCircle(central: Point, converter: DisplaySizeConverter) :
    Circle(
        central,
        converter.convertSampleToDisplay(79f),
        Color.parseColor("#333534"),
        FillPaint.TYPE,
    )

class OuterRim(central: Point, displaySize: Float, converter: DisplaySizeConverter) :
    Circle(
        central,
        displaySize,
        Color.WHITE,
        StrokePaint.TYPE,
        converter.convertSampleToDisplay(1.5f),
    )


class InnerRim(central: Point, displaySize: Float, converter: DisplaySizeConverter) :
    Circle(
        central,
        displaySize,
        Color.parseColor("#070A11"),
        StrokePaint.TYPE,
        converter.convertSampleToDisplay(28.5f),
    )

//Lines
class SmallSerif(converter: DisplaySizeConverter) :
    Line(
        converter.convertSampleToDisplay(4f),
        converter.convertSampleToDisplay(14f),
        converter.convertSampleToDisplay(26.5f),
        Color.WHITE,
        StrokePaint.TYPE
    )

class LargeSerif(converter: DisplaySizeConverter) :
    Line(
        converter.convertSampleToDisplay(8.7f),
        converter.convertSampleToDisplay(36f),
        converter.convertSampleToDisplay(7.5f),
        Color.WHITE,
        StrokePaint.TYPE
    )

class Needle(converter: DisplaySizeConverter) :
    Line(
        converter.convertSampleToDisplay(8.7f),
        converter.convertSampleToDisplay(0f),
        converter.convertSampleToDisplay(7.5f),
        Color.WHITE,
        StrokePaint.TYPE
    )

class Indication(converter: DisplaySizeConverter) :
    Text(
        converter.convertSampleToDisplay(37f),
        converter.convertSampleToDisplay(60f),
        Color.WHITE,
        FillPaint.TYPE
    )


class Hint(converter: DisplaySizeConverter) :
    Text(
        converter.convertSampleToDisplay(32f),
        converter.convertSampleToDisplay(110f),
        Color.WHITE,
        FillPaint.TYPE
    )


