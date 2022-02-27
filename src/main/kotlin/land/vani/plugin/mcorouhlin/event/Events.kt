package land.vani.plugin.mcorouhlin.event

import land.vani.plugin.mcorouhlin.CoroutinePlugin
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@EventDsl
class Events(
    @PublishedApi
    internal val plugin: CoroutinePlugin,
) : Listener {
    inline fun <reified T : Event> on(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        noinline block: suspend (T) -> Unit,
    ) {
        plugin.server.pluginManager
            .registerCoroutineEvent(this, priority, ignoreCancelled, plugin, block)
    }

    inline fun <reified T> cancelIf(
        priority: EventPriority = EventPriority.NORMAL,
        crossinline block: suspend (T) -> Boolean,
    ) where T : Event, T : Cancellable {
        on<T>(priority, true) { event ->
            event.isCancelled = block(event)
        }
    }

    @Suppress("unused")
    inline fun <reified T> cancelIfNot(
        priority: EventPriority = EventPriority.NORMAL,
        crossinline block: suspend (T) -> Boolean,
    ) where T : Event, T : Cancellable {
        cancelIf<T>(priority) { event ->
            block(event).not()
        }
    }
}
