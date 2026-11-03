package io.github.kvmy666.duostatusbar.hook

/** Rive's original signal-dot positions plus the other SIM's concentric inset positions. */
internal object SignalDotsGeometry {
    val OUTER = arrayOf(
        floatArrayOf(-27f, 42.7f), floatArrayOf(-9.5f, 49.7f),
        floatArrayOf(8.5f, 50.2f), floatArrayOf(26f, 44.3f)
    )
    /** All four dots follow the original curved arc, but move toward the ring center. */
    const val INSET_SCALE = 0.72f
    const val INNER_RADIUS = 4.0f
    fun innerX(i: Int): Float = OUTER[i][0] * INSET_SCALE
    fun innerY(i: Int): Float = OUTER[i][1] * INSET_SCALE
}
