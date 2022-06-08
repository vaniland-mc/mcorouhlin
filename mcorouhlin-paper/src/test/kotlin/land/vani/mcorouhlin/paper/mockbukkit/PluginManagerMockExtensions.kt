@file:JvmName("PluginManagerExtensionsKt")

package land.vani.mcorouhlin.paper.mockbukkit

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock
import org.bukkit.event.Event
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
inline fun <reified T : JavaPlugin> PluginManagerMock.loadSimple(parameters: Array<Any> = arrayOf()): T =
    MockBukkit.loadSimple(T::class.java, parameters)

inline fun <reified T : Event> PluginManagerMock.assertEventFired(
    message: String? = null,
    crossinline predicate: (T) -> Boolean = { true },
) {
    if (message != null) {
        assertEventFired(message) { it is T && predicate(it) }
    } else {
        assertEventFired { it is T && predicate(it) }
    }
}
