package io.github.kvmy666.duostatusbar.hook

import android.os.SystemClock
import android.telephony.SubscriptionInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import io.github.kvmy666.duostatusbar.L
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * DEBUG diagnostic only. Observes (never changes) the OEM SystemUI controller's icon output.
 *
 * The OOS16 device report confirms MobileSignalController.getCurrentIconId/getNumLevels.
 * Subscription/state fields are AOSP reference details, not confirmed on every OEM; a missing field
 * must be reported as unknown and must never affect drawing or crash SystemUI.
 */
internal object SystemUiSignalProbe {
    private data class Seen(
        val controller: WeakReference<Any>,
        val iconId: Int,
        val timestampMs: Long
    )
    private val installed = AtomicBoolean(false)
    private val lock = Any()
    private val observations = ArrayList<Seen>()
    @Volatile private var hookStatus = "not installed"

    fun install(loader: ClassLoader) {
        if (!installed.compareAndSet(false, true)) return
        val candidates = listOf(
            "com.android.systemui.statusbar.connectivity.MobileSignalController",
            "com.android.systemui.statusbar.policy.MobileSignalController"
        )
        for (name in candidates) {
            try {
                XposedHelpers.findAndHookMethod(name, loader, "getCurrentIconId",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            try {
                                val owner = param.thisObject ?: return
                                val icon = (param.result as? Number)?.toInt() ?: return
                                synchronized(lock) {
                                    observations.removeAll { it.controller.get() == null }
                                    val index = observations.indexOfFirst { it.controller.get() === owner }
                                    val seen = Seen(WeakReference(owner), icon, SystemClock.elapsedRealtime())
                                    if (index >= 0) observations[index] = seen
                                    else if (observations.size < 8) observations.add(seen)
                                }
                            } catch (_: Throwable) {
                                // A passive probe must never affect SystemUI's normal signal icons.
                            }
                        }
                    })
                hookStatus = "installed: $name.getCurrentIconId"
                L.i("signal diagnostic probe $hookStatus")
                return
            } catch (_: Throwable) {
                // The tested class is absent on this ROM; try only the known AOSP alternate.
            }
        }
        hookStatus = "unavailable: neither documented AOSP MobileSignalController path resolves"
        L.w("signal diagnostic probe $hookStatus")
    }

    fun snapshot(): String = buildString {
        appendLine("  hook=$hookStatus")
        val copies = synchronized(lock) { observations.toList() }
        if (copies.isEmpty()) {
            appendLine("  no icon callbacks observed; compare stock mobile_combo drawable states below")
        }
        for ((index, seen) in copies.withIndex()) {
            val owner = seen.controller.get() ?: continue
            val age = (SystemClock.elapsedRealtime() - seen.timestampMs).coerceAtLeast(0L)
            val sub = runCatching {
                (XposedHelpers.getObjectField(owner, "mSubscriptionInfo") as? SubscriptionInfo)
                    ?.subscriptionId
            }.getOrNull()
            val currentState = runCatching { XposedHelpers.getObjectField(owner, "mCurrentState") }.getOrNull()
            val raw = currentState?.let { runCatching { XposedHelpers.getIntField(it, "level") }.getOrNull() }
            val connected = currentState?.let { runCatching { XposedHelpers.getBooleanField(it, "connected") }.getOrNull() }
            val bins = runCatching { (XposedHelpers.callMethod(owner, "getNumLevels") as? Number)?.toInt() }.getOrNull()
            val packed = SystemUiSignalState.decode(seen.iconId)
            appendLine("  controller#$index subId=${sub ?: "unknown"} ageMs=$age " +
                "icon=0x${seen.iconId.toUInt().toString(16)} " +
                "AOSPpacked=${packed?.let { "${it.level}/${it.numLevels} state=${it.state}" } ?: "not-verified"} " +
                "controllerLevel=${raw ?: "unknown"} getNumLevels=${bins ?: "unknown"} " +
                "connected=${connected ?: "unknown"}")
        }
        appendLine("  interpretation: raw API = TelephonyManager; SystemUI = actual controller " +
            "when packed state and subId are verified. Drawn dots still use the original API.")
    }
}
