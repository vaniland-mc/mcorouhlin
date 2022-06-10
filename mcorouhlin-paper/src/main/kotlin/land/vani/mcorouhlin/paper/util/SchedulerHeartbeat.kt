package land.vani.mcorouhlin.paper.util

import land.vani.mcorouhlin.paper.McorouhlinKotlinPlugin
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

internal fun McorouhlinKotlinPlugin.withSchedulerHeartBeat(block: () -> Unit) {
    manipulatedServerHeartbeatEnabled = true
    block()
    manipulatedServerHeartbeatEnabled = false
}

internal fun McorouhlinKotlinPlugin.ensureWakeup() {
    if (!manipulatedServerHeartbeatEnabled) {
        return
    }

    val primaryThread = if (server.isPrimaryThread) {
        Thread.currentThread()
    } else {
        return
    }

    val thread = Executors.newSingleThreadExecutor()

    if (server.scheduler::class.java.simpleName == "CraftScheduler") {
        thread.submit {
            LockSupport.getBlocker(primaryThread) ?: return@submit

            val currentTickField = server.scheduler::class.java.getField("currentTick")
                .apply {
                    isAccessible = true
                }
            val mainThreadHeartbeatMethod = server.scheduler::class.java
                .getMethod("mainThreadHeartbeat", Int::class.java)
                .apply {
                    isAccessible = true
                }
            val currentTick = currentTickField[server.scheduler]
            mainThreadHeartbeatMethod.invoke(server.scheduler, currentTick)
        }
    }
}
