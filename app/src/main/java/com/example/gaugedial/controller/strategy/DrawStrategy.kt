package com.example.gaugedial.controller.strategy

import android.graphics.Canvas
import com.example.gaugedial.model.Shape

interface DrawStrategy {
  fun draw(canvas: Canvas?, shape: Shape)
}