package com.example.gaugedial.controller.strategy

import android.graphics.Canvas
import com.example.gaugedial.model.Circle
import com.example.gaugedial.model.Shape

class CircleDrawStrategy : DrawStrategy {
    override fun draw(canvas: Canvas?, shape: Shape) {
        val circle = shape as Circle
        canvas?.drawCircle(
            circle.central.x,
            circle.central.y + additional,
            circle.radius,
            circle.brush.paint
        )
    }

    var additional = 0f
    fun update(value: Float) {
        additional = value
    }
}
