package land.vani.plugin.mcorouhlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.bukkit.plugin.Plugin

interface CoroutinePlugin : Plugin, CoroutineScope {
    val mainThreadDispatcher: CoroutineDispatcher
    val asyncDispatcher: CoroutineDispatcher

    suspend fun onEnableAsync()
    suspend fun onDisableAsync()
    suspend fun onLoadAsync()
}
