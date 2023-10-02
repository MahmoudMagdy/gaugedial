package com.example.gaugedial.model

import android.graphics.Paint
import androidx.annotation.IntDef

class Brush(@BrushType private val type: Int, color: Int, width: Float) {
    val paint: Paint = when (type) {
        StrokePaint.TYPE -> {
            StrokePaint(color, width)
        }

        else -> {
            FillPaint(color)
        }
    }
}

sealed class LocalPaint(color: Int) : Paint() {
    init {
        this.color = color
    }
}

class FillPaint(color: Int) : LocalPaint(color) {
    companion object {
        const val TYPE = 0
    }

    init {
        this.style = Style.FILL
    }
}

class StrokePaint(color: Int, width: Float) : LocalPaint(color) {
    companion object {
        const val TYPE = 1
    }

    init {
        this.style = Style.STROKE
        strokeWidth = width
        strokeCap = Cap.ROUND
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    FillPaint.TYPE,
    StrokePaint.TYPE
)
annotation class BrushType
