package com.example.gaugedial.util


/*
sealed class PhysicalAspects(
    val width: Float,
    val height: Float,
    val margin: Float,
    @BrushType brushType: Int,
    color: Int,
) {
    val brush: Paint = Brush(brushType, color, width).paint

    constructor(
        converter: DisplaySizeConverter,
        sampleWidth: Float,
        sampleHeight: Float,
        sampleMargin: Float,
        @BrushType brushType: Int,
        color: Int
    ) : this(
        converter.convertSampleToDisplay(sampleWidth),
        converter.convertSampleToDisplay(sampleHeight),
        converter.convertSampleToDisplay(sampleMargin),
        brushType,
        color
    )
}
*/


/*
class SmallSerif(converter: DisplaySizeConverter) :
    PhysicalAspects(converter, 4f, 14f, 25f, StrokePaint.TYPE, Color.WHITE)

class LargeSerif(converter: DisplaySizeConverter) :
    PhysicalAspects(converter, 8.7f, 36f, 6f, StrokePaint.TYPE, Color.WHITE)

class Indication(converter: DisplaySizeConverter) :
    PhysicalAspects(converter, 37f, 37f, 47f, FillPaint.TYPE, Color.WHITE)

class Hint(converter: DisplaySizeConverter) :
    PhysicalAspects(converter, 37f, 37f, 67f, FillPaint.TYPE, Color.WHITE)
*/

/*
class OuterRim(displaySize: Float, central: Point) :
    PhysicalAspects(displaySize, 1.5, 0f, 0f, StrokePaint.TYPE, Color.WHITE) {
    val circle = Circle(central, displaySize)
}

class InnerRim(displaySize: Float, central: Point) :
    PhysicalAspects(displaySize, 28.5, 0f, 0f, StrokePaint.TYPE, Color.parseColor("#070A11")) {
    val circle = Circle(central, (displaySize - width).toFloat())
}

class InnerCentralCircle(displaySize: Float, central: Point) :
    PhysicalAspects(displaySize, 79f, 79f, 0f, FillPaint.TYPE, Color.parseColor("#333534")) {
    val circle = Circle(central, width.toFloat())
}
*/

//class OuterCentralCircle(displaySize: Float, central: Point) :
//    PhysicalAspects(displaySize, 88f, 88f, 0f, FillPaint.TYPE, Color.parseColor("#E0EDE6")) {
//    val circle = Circle(central, width.toFloat())
//}
//
//class CircularBackground(displaySize: Float, central: Point) :
//    PhysicalAspects(
//        displaySize.toDouble(),
//        displaySize.toDouble(),
//        0f,
//        FillPaint.TYPE,
//        Color.parseColor("#16182D")
//    ) {
//    val circle = Circle(central, displaySize)
//}

class DisplaySizeConverter(private val displaySize: Float) {
    companion object {
        private const val SAMPLE_SIZE = 615
    }

    fun convertSampleToDisplay(value: Float): Float =
        displaySize * (value / SAMPLE_SIZE)
}