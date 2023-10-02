package com.example.gaugedial.controller.strategy

import android.graphics.Canvas
import android.graphics.Paint
import com.example.gaugedial.model.Shape
import com.example.gaugedial.model.Text

class TextDrawStrategy : DrawStrategy {
    private var info: Text.Info? = null

    override fun draw(canvas: Canvas?, shape: Shape) {
        val text = shape as Text
        text.brush.paint.textSize = text.size
        this.info?.also { info ->
            text.brush.paint.textAlign = when {
                info.angle < 247.5f && info.angle >= 150 -> Paint.Align.LEFT
                info.angle in 247.5f..292.5f -> Paint.Align.CENTER
                else -> Paint.Align.RIGHT
            }
            canvas?.drawText(
                info.value,
                info.point.x,
                info.point.y,
                text.brush.paint
            )
        }
    }

    fun update(info: Text.Info) {
        this.info = info
    }
}
