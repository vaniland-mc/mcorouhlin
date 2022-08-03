package land.vani.mcorouhlin.paper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import land.vani.mcorouhlin.command.Command
import land.vani.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import land.vani.mcorouhlin.event.Events
import land.vani.mcorouhlin.permission.PermissionRegisterer
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin

/**
 * Represents a Plugin with mcorouhlin framework.
 *
 * @see McorouhlinKotlinPlugin
 */
interface McorouhlinPlugin : Plugin, CoroutineScope, PermissionRegisterer {
    /**
     * [CoroutineDispatcher] that runs in the Minecraft main thread.
     */
    val mainThreadDispatcher: MinecraftMainThreadDispatcher

    /**
     * [CoroutineDispatcher] that runs in the Minecraft async thread.
     */
    val asyncDispatcher: MinecraftAsyncDispatcher

    /**
     * Called when this plugin is enabled.
     */
    suspend fun onEnableAsync()

    /**
     * Called when this plugin is disabled.
     */
    suspend fun onDisableAsync()

    /**
     * Called when this plugin is load.
     */
    suspend fun onLoadAsync()

    /**
     * Register events with [Events].
     */
    fun events(block: Events<Event>.() -> Unit)

    /**
     * Register commands with [Command].
     */
    suspend fun registerCommand(command: Command<CommandSender>)
}
