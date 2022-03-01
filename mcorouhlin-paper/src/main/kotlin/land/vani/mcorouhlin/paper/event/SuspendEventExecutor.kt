package land.vani.mcorouhlin.paper.event

import co.aikar.timings.Timing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import land.vani.mcorouhlin.extension.invokeSuspend
import land.vani.mcorouhlin.paper.McorouhlinPlugin
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import java.lang.reflect.Method

internal class SuspendEventExecutor<T : Event>(
    private val eventClass: Class<T>,
    private val method: Method,
    private val plugin: McorouhlinPlugin,
    private val timings: Timing,
) : EventExecutor {
    override fun execute(listener: Listener, event: Event) {
        executeSuspend(listener, event)
    }

    fun executeSuspend(listener: Listener, event: Event): Job {
        if (!eventClass.isAssignableFrom(event.javaClass)) {
            return Job()
        }

        val dispatcher = if (event.isAsynchronous) {
            Dispatchers.Unconfined
        } else {
            plugin.mainThreadDispatcher
        }

        return plugin.launch(dispatcher) {
            if (method.isEventHandler) {
                if (!event.isAsynchronous) {
                    timings.startTiming()
                }
                method.invoke(listener, event)
                if (!event.isAsynchronous) {
                    timings.stopTiming()
                }
            } else if (method.isSuspendEventHandler) {
                method.invokeSuspend(listener, event)
            } else {
                throw IllegalArgumentException(
                    "Method does not have the EventHandler annotation or is an invalid method."
                )
            }
        }
    }
}
