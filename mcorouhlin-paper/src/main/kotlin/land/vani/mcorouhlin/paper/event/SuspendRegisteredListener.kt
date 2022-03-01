package land.vani.mcorouhlin.paper.event

import kotlinx.coroutines.Job
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener

internal class SuspendRegisteredListener(
    listener: Listener,
    private val executor: EventExecutor,
    priority: EventPriority,
    plugin: Plugin,
    ignoreCanceled: Boolean,
) : RegisteredListener(listener, executor, priority, plugin, ignoreCanceled) {
    @Suppress("unused")
    fun callSuspendEvent(event: Event): Job {
        if (event is Cancellable) {
            if (event.isCancelled && isIgnoringCancelled) {
                return Job()
            }
        }

        return when (executor) {
            is SuspendEventExecutor<*> -> {
                executor.executeSuspend(listener, event)
            }
            is SuspendEventLambdaExecutor<*> -> {
                executor.executeSuspend(event)
            }
            else -> {
                Job()
            }
        }
    }
}
