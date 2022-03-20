package land.vani.mcorouhlin.paper

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import land.vani.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import land.vani.mcorouhlin.event.Events
import land.vani.mcorouhlin.paper.dispatcher.BukkitMinecraftAsyncDispatcher
import land.vani.mcorouhlin.paper.dispatcher.BukkitMinecraftMainThreadDispatcher
import land.vani.mcorouhlin.paper.event.BukkitEvents
import land.vani.mcorouhlin.paper.permission.asBukkit
import land.vani.mcorouhlin.permission.Permission
import org.bukkit.event.Event
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * Represents a JavaPlugin with mcorouhlin framework.
 */
abstract class McorouhlinKotlinPlugin : JavaPlugin, McorouhlinPlugin {
    constructor() : super()

    constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File,
    ) : super(loader, description, dataFolder, file)

    override val coroutineContext: CoroutineContext by lazy {
        Job() + mainThreadDispatcher
    }

    override val asyncDispatcher: MinecraftAsyncDispatcher by lazy {
        BukkitMinecraftAsyncDispatcher(this)
    }

    override val mainThreadDispatcher: MinecraftMainThreadDispatcher by lazy {
        BukkitMinecraftMainThreadDispatcher(this)
    }

    override suspend fun onEnableAsync() {
        // default empty
    }

    override suspend fun onDisableAsync() {
        // default empty
    }

    override suspend fun onLoadAsync() {
        // default empty
    }

    override fun onEnable() {
        runBlocking {
            onEnableAsync()
        }
    }

    override fun onDisable() {
        runBlocking {
            onDisableAsync()
        }
    }

    override fun onLoad() {
        runBlocking {
            onLoadAsync()
        }
    }

    override fun registerPermissions(permissions: Iterable<Permission>) {
        permissions.forEach { perm ->
            server.pluginManager.addPermission(perm.asBukkit)
        }
    }

    override fun events(block: Events<Event>.() -> Unit) {
        BukkitEvents(this).apply(block)
    }
}
