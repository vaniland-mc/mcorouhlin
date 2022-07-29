package land.vani.mcorouhlin.paper

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import land.vani.mcorouhlin.command.Command
import land.vani.mcorouhlin.command.registerLowercase
import land.vani.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import land.vani.mcorouhlin.event.Events
import land.vani.mcorouhlin.paper.command.BrigadierCommandWrapper
import land.vani.mcorouhlin.paper.dispatcher.BukkitMinecraftAsyncDispatcher
import land.vani.mcorouhlin.paper.dispatcher.BukkitMinecraftMainThreadDispatcher
import land.vani.mcorouhlin.paper.event.BukkitEvents
import land.vani.mcorouhlin.paper.permission.asBukkit
import land.vani.mcorouhlin.paper.util.withSchedulerHeartBeat
import land.vani.mcorouhlin.permission.Permission
import org.bukkit.command.CommandSender
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

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Unconfined

    internal var manipulatedServerHeartbeatEnabled: Boolean = false

    override val asyncDispatcher: MinecraftAsyncDispatcher by lazy {
        BukkitMinecraftAsyncDispatcher(this)
    }

    override val mainThreadDispatcher: MinecraftMainThreadDispatcher by lazy {
        BukkitMinecraftMainThreadDispatcher(this)
    }

    private val commandDispatcher: CommandDispatcher<CommandSender> = CommandDispatcher()

    @Suppress("MagicNumber")
    private val commandCache: Cache<Pair<String, CommandSender>, ParseResults<CommandSender>> =
        CacheBuilder.newBuilder()
            .maximumSize(10)
            .build()

    private val commandMutex = Mutex()

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
        withSchedulerHeartBeat {
            runBlocking(Dispatchers.Unconfined) {
                onEnableAsync()
            }
        }
    }

    override fun onDisable() {
        runBlocking(Dispatchers.Unconfined) {
            onDisableAsync()
        }
    }

    override fun onLoad() {
        runBlocking(Dispatchers.Unconfined) {
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

    override fun registerCommand(command: Command<CommandSender>) {
        val bukkitCommand = BrigadierCommandWrapper(
            command.literal.lowercase(),
            this,
            commandDispatcher,
            commandCache,
        )

        launch {
            commandMutex.withLock {
                server.commandMap.register(name, bukkitCommand)
                commandDispatcher.registerLowercase(command)
            }
        }
    }
}
