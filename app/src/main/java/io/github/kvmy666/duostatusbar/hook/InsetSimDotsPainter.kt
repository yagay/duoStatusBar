package io.github.kvmy666.duostatusbar.hook

import android.graphics.Canvas
import android.graphics.Paint
import io.github.kvmy666.duostatusbar.L

/**
 * Four extra circles drawn by the existing DuoRiveView FrameLayout, after its TextureView child.
 * No second MATCH_PARENT child: it cannot alter the measured icon size or resize the Rive surface.
 */
internal class InsetSimDotsPainter {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var color = 0xFFFFFFFF.toInt()
    private var opacities = floatArrayOf(0f, 0f, 0f, 0f)

    /** Returns true only when the actual drawn pixels need updating. */
    fun setVisual(visual: DuoVisual): Boolean {
        val next = floatArrayOf(
            visual.innerCell1Opacity, visual.innerCell2Opacity,
            visual.innerCell3Opacity, visual.innerCell4Opacity
        )
        if (visual.ringOpacity <= 0.5f || !visual.visible) next.fill(0f)
        if (color == visual.fgColor && opacities.contentEquals(next)) return false
        color = visual.fgColor
        opacities = next
        return true
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        if (opacities.all { it <= 0f }) return
        try {
            val frame = DuoArtboard.frame(width, height)
            if (frame.scale <= 0f) return
            for (i in opacities.indices) {
                val opacity = opacities[i]
                if (opacity <= 0f) continue
                paint.color = color
                paint.alpha = (opacity * 255f).toInt().coerceIn(0, 255)
                canvas.drawCircle(
                    frame.centerX + SignalDotsGeometry.innerX(i) * frame.scale,
                    frame.centerY + SignalDotsGeometry.innerY(i) * frame.scale,
                    SignalDotsGeometry.INNER_RADIUS * frame.scale,
                    paint
                )
            }
        } catch (t: Throwable) {
            L.w("inset SIM dots: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
