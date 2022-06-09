package land.vani.mcorouhlin.paper.util

import land.vani.mcorouhlin.paper.McorouhlinKotlinPlugin
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

internal fun withSchedulerHeartBeat(plugin: McorouhlinKotlinPlugin, block: () -> Unit) {
    val primaryThread = if (plugin.server.isPrimaryThread) {
        Thread.currentThread()
    } else {
        return
    }

    val thread = Executors.newSingleThreadExecutor()

    if (plugin.server.scheduler::class.java.simpleName == "CraftScheduler") {
        thread.submit {
            LockSupport.getBlocker(primaryThread) ?: return@submit

            val currentTickField = plugin.server.scheduler::class.java.getField("currentTick")
                .apply {
                    isAccessible = true
                }
            val mainThreadHeartbeatMethod = plugin.server.scheduler::class.java
                .getMethod("mainThreadHeartbeat", Int::class.java)
                .apply {
                    isAccessible = true
                }
            val currentTick = currentTickField[plugin.server.scheduler]
            mainThreadHeartbeatMethod.invoke(plugin.server.scheduler, currentTick)
        }
    }

    block()

    thread.shutdown()
}
