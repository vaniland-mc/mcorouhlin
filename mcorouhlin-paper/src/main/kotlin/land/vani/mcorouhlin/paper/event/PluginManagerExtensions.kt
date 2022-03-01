package land.vani.mcorouhlin.paper.event

import co.aikar.timings.Timings
import com.destroystokyo.paper.event.server.ServerExceptionEvent
import com.destroystokyo.paper.exception.ServerEventException
import kotlinx.coroutines.Job
import land.vani.mcorouhlin.paper.McorouhlinPlugin
import org.bukkit.Bukkit
import org.bukkit.Warning
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.RegisteredListener
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

/**
 * Registers all the events in the given [listener] class.
 */
fun PluginManager.registerSuspendEvents(listener: Listener, plugin: McorouhlinPlugin) {
    if (!plugin.isEnabled) {
        throw IllegalPluginAccessException("Plugin attempted to register suspend lister $listener while not enabled")
    }

    val registeredListeners = createRegisteredSuspendListener(listener, plugin)
    registeredListeners.forEach { (clazz, listeners) ->
        val handlerList = getEventListeners(clazz)
        handlerList.registerAll(listeners)
    }
}

/**
 * Registers the specified executor to the given event class.
 *
 * @param event type of event to register
 * @param listener listener to register
 * @param priority priority to register this event at
 * @param plugin plugin to register
 */
fun <T : Event> PluginManager.registerSuspendEvent(
    event: KClass<T>,
    listener: Listener,
    priority: EventPriority,
    ignoreCanceled: Boolean,
    plugin: McorouhlinPlugin,
    handler: suspend (T) -> Unit,
) {
    val executor = SuspendEventLambdaExecutor(event.java, handler, plugin)
    registerEvent(event.java, listener, priority, executor, plugin, ignoreCanceled)
}

/**
 * Registers the specified executor to the given event class.
 *
 * @param T type of event to register
 * @param listener listener to register
 * @param priority priority to register this event at
 * @param plugin plugin to register
 */
inline fun <reified T : Event> PluginManager.registerSuspendEvent(
    listener: Listener,
    priority: EventPriority,
    ignoreCanceled: Boolean,
    plugin: McorouhlinPlugin,
    noinline handler: suspend (T) -> Unit,
) = registerSuspendEvent(T::class, listener, priority, ignoreCanceled, plugin, handler)

/**
 * Calls an event with the given details.
 *
 * @param event Event details
 * @throws IllegalStateException Thrown when an asynchronous event is fired from synchronous code.
 */
@Suppress("unused")
fun PluginManager.callSuspendEvent(event: Event): Collection<Job> {
    val server = Bukkit.getServer()

    check(!(event.isAsynchronous && server.isPrimaryThread)) {
        "${event.eventName} may only be triggered asynchronously."
    }
    check(!(!event.isAsynchronous && !server.isPrimaryThread && !server.isStopping)) {
        "${event.eventName} may only be triggered synchronously."
    }

    return event.handlers.registeredListeners
        .filter { it.plugin.isEnabled }
        .mapNotNull {
            try {
                if (it is SuspendRegisteredListener) {
                    it.callSuspendEvent(event)
                } else {
                    it.callEvent(event)
                    null
                }
            } catch (ex: AuthorNagException) {
                if (it.plugin.isNaggable) {
                    it.plugin.isNaggable = false
                    server.logger.log(
                        Level.SEVERE,
                        "Nag author(s): '%s' of '%s' about the following: %s".format(
                            it.plugin.description.authors,
                            it.plugin.description.fullName,
                            ex.message
                        ),
                    )
                }
                null
            } catch (ex: Throwable) {
                val msg = "Could not pass event ${event.eventName} to ${it.plugin.description.fullName}"
                server.logger.log(Level.SEVERE, msg, ex)
                if (event !is ServerExceptionEvent) { // We don't want to cause an endless event loop
                    callEvent(ServerExceptionEvent(ServerEventException(msg, ex, it.plugin, it.listener, event)))
                }
                null
            }
        }
}

private fun createRegisteredSuspendListener(
    listener: Listener,
    plugin: McorouhlinPlugin,
): Map<Class<out Event>, Set<RegisteredListener>> {
    val result = mutableMapOf<Class<out Event>, MutableSet<RegisteredListener>>()

    setOf(*listener.javaClass.methods, *listener.javaClass.declaredMethods)
        .asSequence()
        .filterNotNull()
        .filterNot { it.getAnnotation(EventHandler::class.java) == null }
        .filterNot { it.isBridge || it.isSynthetic }
        .onEach { it.isAccessible = true }
        .filter { method ->
            if (method.isEventHandler || method.isSuspendEventHandler) {
                true
            } else {
                plugin.logger.severe(
                    "${plugin.description.fullName} attempted to register " +
                        "an invalid SuspendEventHandler method signature."
                )
                false
            }
        }
        .map { method -> method to method.getAnnotation(EventHandler::class.java)!! }
        .map { (method, eventHandler) ->
            val eventClass: Class<out Event> = method.parameterTypes[0].asSubclass(Event::class.java)
            var clazz: Class<*> = eventClass
            while (Event::class.java.isAssignableFrom(clazz)) {
                if (clazz.getAnnotation(Deprecated::class.java) == null) {
                    clazz = clazz.superclass
                    continue
                }

                val warning = clazz.getAnnotation(Warning::class.java)
                val warningState = plugin.server.warningState
                if (!warningState.printFor(warning)) {
                    break
                }

                val message = "\"%s\" has registered a listener for %s on method \"%s\", " +
                    "but the event is Deprecated. \"%s\"; please notify the authors %s.".format(
                        plugin.description.fullName,
                        clazz.name,
                        method.toGenericString(),
                        if (warning?.reason?.isNotEmpty() == true) {
                            warning.reason
                        } else {
                            "Server performance will be affected"
                        },
                        plugin.description.authors.joinToString()
                    )

                plugin.logger.log(
                    Level.WARNING,
                    message,
                    if (warningState == Warning.WarningState.ON) {
                        AuthorNagException(null)
                    } else null
                )
            }
            Triple(method, eventClass, eventHandler)
        }
        .forEach { (method, eventClass, eventHandler) ->
            val timings = Timings.of(
                plugin,
                "Event: ${listener.javaClass.name}::${method.name}(${eventClass.simpleName})",
            )

            val executor = SuspendEventExecutor(eventClass, method, plugin, timings)
            result.getOrPut(eventClass) { mutableSetOf() }
                .add(
                    SuspendRegisteredListener(
                        listener,
                        executor,
                        eventHandler.priority,
                        plugin,
                        eventHandler.ignoreCancelled
                    )
                )
        }

    return result
}

internal val Method.isEventHandler: Boolean
    get() = parameterTypes.size == 1 && Event::class.java.isAssignableFrom(parameterTypes[0])

internal val Method.isSuspendEventHandler: Boolean
    get() = parameterTypes.size == 2 && Event::class.java.isAssignableFrom(parameterTypes[0]) &&
        Continuation::class.java.isAssignableFrom(parameterTypes[1])

private fun PluginManager.getEventListeners(type: Class<out Event>): HandlerList =
    this.javaClass.getDeclaredMethod("getEventListeners", Class::class.java).apply {
        isAccessible = true
    }(this, type) as HandlerList
