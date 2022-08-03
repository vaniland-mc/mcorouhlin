package land.vani.mcorouhlin.paper.util

import land.vani.mcorouhlin.paper.McorouhlinKotlinPlugin
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

internal fun McorouhlinKotlinPlugin.withSchedulerHeartBeat(block: () -> Unit) {
    schedulerSupport.manipulatedServerHeartbeatEnabled = true
    block()
    schedulerSupport.manipulatedServerHeartbeatEnabled = false
}

class SchedulerSupport {
    internal var manipulatedServerHeartbeatEnabled: Boolean = false
    internal var primaryThread: Thread? = null
    internal var threadSupport: ExecutorService? = null
}

internal fun McorouhlinKotlinPlugin.ensureWakeup() {
    if (!schedulerSupport.manipulatedServerHeartbeatEnabled) {
        schedulerSupport.threadSupport?.shutdown().also {
            schedulerSupport.threadSupport = null
        }
        return
    }

    if (schedulerSupport.primaryThread == null && server.isPrimaryThread) {
        schedulerSupport.primaryThread = Thread.currentThread()
    }

    if (schedulerSupport.primaryThread == null) {
        return
    }

    if (schedulerSupport.threadSupport == null) {
        schedulerSupport.threadSupport = Executors.newSingleThreadExecutor()
    }

    if (server.scheduler::class.java.simpleName == "CraftScheduler") {
        schedulerSupport.threadSupport!!.submit {
            LockSupport.getBlocker(schedulerSupport.primaryThread) ?: return@submit

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
