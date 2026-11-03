package io.github.kvmy666.duostatusbar.hook

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DualSimInsetDotsTest {
    private fun visual(inner: Int? = null) = DuoMapping.visual(
        level = 50, charging = false, saver = false, showPercent = true,
        wifiLevel = 3, cellLevel = 4, airplane = false, innerCellLevel = inner
    )

    @Test fun singleSimKeepsInsetDotsHidden() {
        val v = visual()
        assertEquals(0f, v.innerCell1Opacity, 0f)
        assertEquals(0f, v.innerCell2Opacity, 0f)
        assertEquals(0f, v.innerCell3Opacity, 0f)
        assertEquals(0f, v.innerCell4Opacity, 0f)
    }

    @Test fun innerDotsShowSecondSignalLevel() {
        val v = visual(2)
        assertEquals(1f, v.innerCell1Opacity, 0f)
        assertEquals(1f, v.innerCell2Opacity, 0f)
        assertEquals(0.3f, v.innerCell3Opacity, 0f)
        assertEquals(0.3f, v.innerCell4Opacity, 0f)
    }

    @Test fun dotsFollowSameArcTowardCircleCenter() {
        for (i in 0..3) {
            val outer = SignalDotsGeometry.OUTER[i]
            val x = SignalDotsGeometry.innerX(i)
            val y = SignalDotsGeometry.innerY(i)
            assertTrue(x * x + y * y < outer[0] * outer[0] + outer[1] * outer[1])
            assertEquals(outer[0] * 0.72f, x, 0.001f)
            assertEquals(outer[1] * 0.72f, y, 0.001f)
        }
    }
}
