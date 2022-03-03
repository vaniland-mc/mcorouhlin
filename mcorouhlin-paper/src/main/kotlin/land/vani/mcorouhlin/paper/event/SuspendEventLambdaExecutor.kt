package land.vani.mcorouhlin.paper.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import land.vani.mcorouhlin.paper.McorouhlinPlugin
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor

internal class SuspendEventLambdaExecutor<T : Event>(
    private val eventClass: Class<T>,
    private val handler: suspend (T) -> Unit,
    private val plugin: McorouhlinPlugin,
) : EventExecutor {
    override fun execute(_listener: Listener, event: Event) {
        executeEvent(event)
    }

    fun executeSuspend(event: Event): Job = executeEvent(event)

    private fun executeEvent(event: Event): Job {
        if (!eventClass.isAssignableFrom(event.javaClass)) {
            return Job()
        }

        val dispatcher = if (event.isAsynchronous) {
            Dispatchers.Unconfined
        } else {
            plugin.mainThreadDispatcher
        }

        return plugin.launch(dispatcher) {
            handler(eventClass.cast(event))
        }
    }
}
