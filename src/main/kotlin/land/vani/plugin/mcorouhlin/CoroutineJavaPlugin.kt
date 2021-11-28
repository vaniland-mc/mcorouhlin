package land.vani.plugin.mcorouhlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import land.vani.plugin.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.plugin.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

abstract class CoroutineJavaPlugin: JavaPlugin(), CoroutinePlugin, CoroutineScope {
    override val coroutineContext: CoroutineContext by lazy {
        Job() + mainThreadDispatcher
    }

    override val mainThreadDispatcher: CoroutineDispatcher by lazy {
        MinecraftMainThreadDispatcher(this)
    }
    override val asyncDispatcher: CoroutineDispatcher by lazy {
        MinecraftAsyncDispatcher(this)
    }

    override fun onEnable() {
        runBlocking(coroutineContext) {
            onEnableAsync()
        }
    }

    override fun onDisable() {
        runBlocking(coroutineContext) {
            onDisableAsync()
            cancel()
        }
    }

    override fun onLoad() {
        runBlocking(coroutineContext) {
            onLoadAsync()
        }
    }
}
