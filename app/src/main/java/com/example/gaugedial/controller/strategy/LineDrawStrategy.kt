package com.example.gaugedial.controller.strategy

import android.graphics.Canvas
import com.example.gaugedial.model.Line
import com.example.gaugedial.model.Shape

class LineDrawStrategy : DrawStrategy {
    var segment: Line.Segment? = null

    override fun draw(canvas: Canvas?, shape: Shape) {
        val line = shape as Line
        this.segment?.also { segment ->
            canvas?.drawLine(
                segment.start.x,
                segment.start.y,
                segment.end.x,
                segment.end.y,
                line.brush.paint
            )
        }
    }

    fun update(segment: Line.Segment) {
        this.segment = segment
    }
}
