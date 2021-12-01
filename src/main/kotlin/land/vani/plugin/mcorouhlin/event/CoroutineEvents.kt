package land.vani.plugin.mcorouhlin.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import land.vani.plugin.mcorouhlin.CoroutinePlugin
import land.vani.plugin.mcorouhlin.extension.invokeSuspend
import org.bukkit.Warning
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.IllegalPluginAccessException
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.RegisteredListener
import org.bukkit.plugin.java.JavaPluginLoader
import org.spigotmc.CustomTimingsHandler
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

@Suppress("unused")
fun PluginManager.registerCoroutineEvents(listener: Listener, plugin: CoroutinePlugin) {
    if (!plugin.isEnabled) {
        throw IllegalPluginAccessException("Plugin attempted to register suspend lister $listener while not enabled")
    }
    val registeredListeners = createRegisteredCoroutineListener(listener, plugin)

    registeredListeners.forEach { (clazz, listeners) ->
        @Suppress("UNCHECKED_CAST")
        val handlerList = getEventListeners(clazz)
        handlerList.registerAll(listeners)
    }
}

fun <T : Event> PluginManager.registerCoroutineEvent(
    event: KClass<T>,
    listener: Listener,
    priority: EventPriority,
    ignoreCanceled: Boolean,
    plugin: CoroutinePlugin,
    handler: suspend (T) -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    val executor = CoroutineEventLambdaExecutor(event.java, handler as suspend (Any) -> Unit, plugin)
    getEventListeners(event.java).register(
        CoroutineRegisteredListener(
            listener,
            executor,
            priority,
            plugin,
            ignoreCanceled
        )
    )
}

inline fun <reified T : Event> PluginManager.registerCoroutineEvent(
    listener: Listener,
    priority: EventPriority,
    ignoreCanceled: Boolean,
    plugin: CoroutinePlugin,
    noinline handler: suspend (T) -> Unit,
) {
    registerCoroutineEvent(T::class, listener, priority, ignoreCanceled, plugin, handler)
}

@Suppress("unused")
fun fireCoroutineEvents(event: Event): Collection<Job> = event.handlers.registeredListeners
    .filter { it.plugin.isEnabled }
    .mapNotNull {
        try {
            if (it is CoroutineRegisteredListener) {
                it.callSuspendEvent(event)
            } else {
                it.callEvent(event)
                null
            }
        } catch (ex: Throwable) {
            it.plugin.logger.log(
                Level.SEVERE,
                "Could not pass event ${event.eventName} to ${it.plugin.description.fullName}",
                ex
            )
            null
        }
    }

private fun createRegisteredCoroutineListener(
    listener: Listener,
    plugin: CoroutinePlugin,
): Map<Class<out Event>, Set<RegisteredListener>> {
    val result = mutableMapOf<Class<out Event>, MutableSet<RegisteredListener>>()

    setOf(*listener.javaClass.methods, *listener.javaClass.declaredMethods)
        .asSequence()
        .filterNotNull()
        .filterNot { it.getAnnotation(EventHandler::class.java) == null }
        .filterNot { it.isBridge || it.isSynthetic }
        .onEach { it.isAccessible = true }
        .filter { method ->
            (method.isEventHandler || method.isSuspendEventHandler).also {
                if (!it) {
                    plugin.logger.severe("${plugin.description.fullName} attempted to register an invalid SuspendEventHandler method signature.")
                }
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

                plugin.logger.log(
                    Level.WARNING,
                    """"%s" has registered a listener for %s on method "%s", but the event is Deprecated. "%s"; please notify the authors %s.""".format(
                        plugin.description.fullName,
                        clazz.name,
                        method.toGenericString(),
                        if (warning?.reason?.isNotEmpty() == true) warning.reason else "Server performance will be affected",
                        plugin.description.authors.joinToString()
                    ),
                    if (warningState == Warning.WarningState.ON) {
                        AuthorNagException(null)
                    } else null
                )
            }
            Triple(method, eventClass, eventHandler)
        }
        .forEach { (method, eventClass, eventHandler) ->
            val timings = CustomTimingsHandler(
                "Plugin: ${plugin.description.fullName} Event: ${listener.javaClass.name}::${method.name}(${eventClass.simpleName})",
                JavaPluginLoader.pluginParentTimer
            )

            @Suppress("UNCHECKED_CAST")
            val executor = CoroutineEventExecutor(eventClass, method, plugin, timings)
            result.getOrPut(eventClass) { mutableSetOf() }
                .add(
                    CoroutineRegisteredListener(
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

internal class CoroutineEventExecutor<T : Event>(
    private val eventClass: Class<T>,
    private val method: Method,
    private val plugin: CoroutinePlugin,
    private val timings: CustomTimingsHandler,
) : EventExecutor {
    override fun execute(listener: Listener, event: Event) {
        executeEvent(listener, event)
    }

    fun executeSuspend(listener: Listener, event: Event): Job = executeEvent(listener, event)

    private fun executeEvent(listener: Listener, event: Event): Job {
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
                throw IllegalArgumentException("Method does not have the EventHandler annotation or is an invalid method.")
            }
        }
    }
}

internal class CoroutineEventLambdaExecutor<T : Event>(
    private val eventClass: Class<T>,
    private val handler: suspend (T) -> Unit,
    private val plugin: CoroutinePlugin,
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
            @Suppress("UNCHECKED_CAST")
            handler(event as T)
        }
    }
}

class CoroutineRegisteredListener(
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
            is CoroutineEventExecutor<*> -> {
                executor.executeSuspend(listener, event)
            }
            is CoroutineEventLambdaExecutor<*> -> {
                executor.executeSuspend(event)
            }
            else -> {
                Job()
            }
        }
    }
}

private val Method.isEventHandler: Boolean
    get() = parameterTypes.size == 1 && Event::class.java.isAssignableFrom(parameterTypes[0])

private val Method.isSuspendEventHandler: Boolean
    get() = parameterTypes.size == 2 && Event::class.java.isAssignableFrom(parameterTypes[0])
            && Continuation::class.java.isAssignableFrom(parameterTypes[1])

private fun PluginManager.getEventListeners(type: Class<out Event>): HandlerList =
    this.javaClass.getDeclaredMethod("getEventListeners", Class::class.java).apply {
        isAccessible = true
    }(this, type) as HandlerList
