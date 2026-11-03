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

    @Test fun innerAndOuterDotsAreTheSameSize() {
        assertEquals(5.5f, SignalDotsGeometry.INNER_RADIUS, 0f)
        assertEquals(SignalDotsGeometry.DOT_RADIUS, SignalDotsGeometry.INNER_RADIUS, 0f)
    }

    @Test fun concentricDotsDoNotOverlap() {
        for (i in 0..3) for (j in 0..3) {
            val dx = SignalDotsGeometry.OUTER[i][0] - SignalDotsGeometry.innerX(j)
            val dy = SignalDotsGeometry.OUTER[i][1] - SignalDotsGeometry.innerY(j)
            assertTrue(dx * dx + dy * dy > 4f * SignalDotsGeometry.DOT_RADIUS * SignalDotsGeometry.DOT_RADIUS)
        }
    }

    @Test fun canvasFrameMatchesRiveArtboard() {
        val f = DuoArtboard.frame(120, 136)
        assertEquals(1f, f.scale, 0.0001f)
        assertEquals(60f, f.centerX, 0.0001f)
        assertEquals(77.5f, f.centerY, 0.0001f)
        assertEquals(51.5f, DuoArtboard.RING_RADIUS, 0.0001f)
        assertEquals(8f, DuoArtboard.RING_STROKE, 0.0001f)
    }

    @Test fun canvasRingFitsInsideTheArtboardWithoutClipping() {
        val f = DuoArtboard.frame(80, 91)
        val ink = (DuoArtboard.RING_RADIUS + DuoArtboard.RING_STROKE / 2f) * f.scale
        assertTrue(f.centerX - ink >= 0f)
        assertTrue(f.centerX + ink <= 80f)
        val dotBottom = f.centerY +
            (SignalDotsGeometry.OUTER.maxOf { it[1] } + SignalDotsGeometry.DOT_RADIUS) * f.scale
        assertTrue(dotBottom <= 91f)
    }

    @Test fun ringArcEndsAreSymmetric() {
        val leftBottom = RingGeometry.LEFT_START
        val rightBottom = RingGeometry.RIGHT_FULL
        assertEquals(1f, leftBottom + rightBottom, 0.00001f)
        for (charging in listOf(false, true)) {
            val leftTop = RingGeometry.LEFT_START + RingGeometry.halfArc(charging)
            val rightTop = if (charging) RingGeometry.GAP_CHARGING_DEG / 720f
                else RingGeometry.RIGHT_START
            assertEquals(1f, leftTop + rightTop, 0.0001f)
        }
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
