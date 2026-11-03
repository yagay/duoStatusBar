package io.github.kvmy666.duostatusbar.hook

/** Rive's measured artboard and the identical scaling used by the Canvas fallback/second SIM. */
internal object DuoArtboard {
    const val WIDTH = 120f
    const val HEIGHT = 136f
    const val CENTER_Y = 77.5f
    const val RING_DIAMETER = 103f
    const val RING_RADIUS = RING_DIAMETER / 2f
    const val RING_STROKE = 8f

    data class Frame(val scale: Float, val centerX: Float, val centerY: Float)

    /** Fit.CONTAIN, Alignment.CENTER; both renderers use the exact same local origin and scale. */
    fun frame(width: Int, height: Int): Frame {
        if (width <= 0 || height <= 0) return Frame(0f, 0f, 0f)
        val scale = minOf(width / WIDTH, height / HEIGHT)
        return Frame(scale, width / 2f, height / 2f + (CENTER_Y - HEIGHT / 2f) * scale)
    }
}
