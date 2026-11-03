package io.github.kvmy666.duostatusbar.hook

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import io.github.kvmy666.duostatusbar.L

/** Optional extra four SIM circles composed over the unchanged Rive animation. */
internal class InsetSimDotsView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var color = 0xFFFFFFFF.toInt()
    private var levels = floatArrayOf(0f, 0f, 0f, 0f)

    init { isClickable = false; isFocusable = false }

    fun setVisual(visual: DuoVisual) {
        val next = floatArrayOf(
            visual.innerCell1Opacity, visual.innerCell2Opacity,
            visual.innerCell3Opacity, visual.innerCell4Opacity
        )
        if (visual.ringOpacity <= 0.5f) next.fill(0f)
        if (color == visual.fgColor && levels.contentEquals(next)) return
        color = visual.fgColor
        levels = next
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        try {
            // Exactly Rive's Fit.CONTAIN, centered 120x136 artboard and (60,77.5) ring center.
            val scale = minOf(width / ARTBOARD_WIDTH, height / ARTBOARD_HEIGHT)
            if (scale <= 0f) return
            val cx = width / 2f
            val cy = height / 2f + (RING_CENTER_Y - ARTBOARD_HEIGHT / 2f) * scale
            for (i in levels.indices) {
                val opacity = levels[i]
                if (opacity <= 0f) continue
                paint.color = color
                paint.alpha = (opacity * 255f).toInt().coerceIn(0, 255)
                canvas.drawCircle(
                    cx + SignalDotsGeometry.innerX(i) * scale,
                    cy + SignalDotsGeometry.innerY(i) * scale,
                    SignalDotsGeometry.INNER_RADIUS * scale, paint
                )
            }
        } catch (t: Throwable) {
            L.w("inset SIM dots draw: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private companion object {
        const val ARTBOARD_WIDTH = 120f
        const val ARTBOARD_HEIGHT = 136f
        const val RING_CENTER_Y = 77.5f
    }
}
