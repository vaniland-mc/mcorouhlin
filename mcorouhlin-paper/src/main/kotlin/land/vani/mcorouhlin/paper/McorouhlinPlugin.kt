package land.vani.mcorouhlin.paper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import land.vani.mcorouhlin.permission.PermissionRegisterer
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
    val mainThreadDispatcher: CoroutineDispatcher

    /**
     * [CoroutineDispatcher] that runs in the Minecraft async thread.
     */
    val asyncDispatcher: CoroutineDispatcher

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
}