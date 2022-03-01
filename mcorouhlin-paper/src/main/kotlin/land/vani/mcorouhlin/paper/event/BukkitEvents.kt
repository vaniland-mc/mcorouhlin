package land.vani.mcorouhlin.paper.event

import land.vani.mcorouhlin.event.EventPriority
import land.vani.mcorouhlin.event.Events
import land.vani.mcorouhlin.paper.McorouhlinPlugin
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import kotlin.reflect.KClass

internal class BukkitEvents(
    private val plugin: McorouhlinPlugin,
) : Events<Event>, Listener {
    override fun <T : Event> on(
        clazz: KClass<T>,
        priority: EventPriority,
        ignoreCancelled: Boolean,
        block: suspend (T) -> Unit,
    ) {
        plugin.server.pluginManager.registerSuspendEvent(
            clazz,
            this,
            priority.asBukkit,
            ignoreCancelled,
            plugin,
            block,
        )
    }
}

/**
 * Register event handler.
 *
 * @param T type of the event.
 * @param priority priority of event handler.
 * @param ignoreCancelled ignore when an event has been canceled.
 */
inline fun <reified T : Event> Events<Event>.on(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline block: suspend (T) -> Unit,
) {
    on(T::class, priority, ignoreCancelled, block)
}

/**
 * Register cancelling event handler.
 *
 * @param T type of the event.
 * @param priority priority of event handler.
 */
inline fun <reified T> Events<Event>.cancelIf(
    priority: EventPriority = EventPriority.NORMAL,
    noinline block: suspend (T) -> Boolean,
) where T : Event, T : Cancellable {
    on<T>(priority, true) {
        it.isCancelled = block(it)
    }
}

/**
 * Register unless cancelling event handler.
 *
 * @param T type of the event.
 * @param priority priority of event handler.
 */
inline fun <reified T> Events<Event>.cancelIfNot(
    priority: EventPriority = EventPriority.NORMAL,
    noinline block: suspend (T) -> Boolean,
) where T : Event, T : Cancellable {
    on<T>(priority, true) {
        it.isCancelled = !block(it)
    }
}
