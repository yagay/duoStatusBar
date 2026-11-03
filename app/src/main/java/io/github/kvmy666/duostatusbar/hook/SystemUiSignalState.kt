package io.github.kvmy666.duostatusbar.hook

/** AOSP SignalDrawable integer state, not an icon-resource ID. Invalid/unknown formats stay unknown. */
internal object SystemUiSignalState {
    data class Packed(val level: Int, val numLevels: Int, val state: Int)

    fun decode(iconId: Int): Packed? {
        // AOSP: low 8 bits = level, next 8 = number of bins, next 8 = state.
        // A vendor's resource ID must not accidentally turn into a signal reading.
        if (iconId ushr 24 != 0) return null
        val level = iconId and 0xff
        val bins = (iconId ushr 8) and 0xff
        val state = (iconId ushr 16) and 0xff
        if (bins !in 4..7 || level >= bins || state !in 0..2) return null
        return Packed(level, bins, state)
    }
}
