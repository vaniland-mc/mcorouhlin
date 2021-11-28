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
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.plugin.java.JavaPluginLoader
import org.spigotmc.CustomTimingsHandler
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.coroutines.Continuation

fun PluginManager.registerCoroutineEvents(listener: Listener, plugin: CoroutinePlugin) {
    if (!plugin.isEnabled) {
        throw IllegalPluginAccessException("Plugin attempted to register suspend lister $listener while not enabled")
    }
    val registeredListeners = createRegisteredCoroutineListener(listener, plugin)

    val method = SimplePluginManager::class.java
        .getDeclaredMethod("getEventListeners", Class::class.java).apply {
            isAccessible = true
        }

    registeredListeners.forEach { (clazz, listeners) ->
        val handlerList = method.invoke(plugin.server.pluginManager, clazz) as HandlerList
        handlerList.registerAll(listeners)
    }
}

fun PluginManager.fireCoroutineEvents(event: Event): Collection<Job> = event.handlers.registeredListeners
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
    plugin: CoroutinePlugin
): Map<Class<*>, Set<RegisteredListener>> {
    val result = mutableMapOf<Class<*>, MutableSet<RegisteredListener>>()

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
            var eventClass: Class<*> = method.parameterTypes[0].asSubclass(Event::class.java)
            while (Event::class.java.isAssignableFrom(eventClass)) {
                if (eventClass.getAnnotation(Deprecated::class.java) == null) {
                    eventClass = eventClass.superclass
                    continue
                }

                val warning = eventClass.getAnnotation(Warning::class.java)
                val warningState = plugin.server.warningState
                if(!warningState.printFor(warning)) {
                    break
                }

                plugin.logger.log(
                    Level.WARNING,
                    """"%s" has registered a listener for %s on method "%s", but the event is Deprecated. "%s"; please notify the authors %s.""".format(
                        plugin.description.fullName,
                        eventClass.name,
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

class CoroutineEventExecutor(
    private val eventClass: Class<*>,
    private val method: Method,
    private val plugin: CoroutinePlugin,
    private val timings: CustomTimingsHandler,
): EventExecutor {
    override fun execute(listener: Listener, event: Event) {
        executeEvent(listener, event)
    }

    fun executeSuspend(listener: Listener, event: Event): Job = executeEvent(listener, event)

    private fun executeEvent(listener: Listener, event: Event): Job {
        if (! eventClass.isAssignableFrom(event.javaClass)) {
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

class CoroutineRegisteredListener(
    listener: Listener,
    private val executor: EventExecutor,
    priority: EventPriority,
    plugin: Plugin,
    ignoreCanceled: Boolean
): RegisteredListener(listener, executor, priority, plugin, ignoreCanceled) {
    fun callSuspendEvent(event: Event): Job {
        if (event is Cancellable) {
            if (event.isCancelled && isIgnoringCancelled) {
                return Job()
            }
        }

        return if (executor is CoroutineEventExecutor) {
            executor.executeSuspend(listener, event)
        } else {
            executor.execute(listener, event)
            Job()
        }
    }
}

private val Method.isEventHandler: Boolean
    get() = parameterTypes.size == 1 && Event::class.java.isAssignableFrom(parameterTypes[0])

private val Method.isSuspendEventHandler: Boolean
    get() = parameterTypes.size == 2 && Event::class.java.isAssignableFrom(parameterTypes[0])
            && Continuation::class.java.isAssignableFrom(parameterTypes[1])
